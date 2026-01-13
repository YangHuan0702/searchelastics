package org.halosky.storage;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.halosky.config.Config;
import org.halosky.handler.IndexSetting;
import org.halosky.query.QueryNode;
import org.halosky.query.QueryParse;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * packageName org.halosky.storage
 *
 * @author huan.yang
 * @className IndexManager
 * @date 2026/1/12
 */
@Slf4j
public class StorageManager {

    private final Config config;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, IndexMetadata> indexMap = new ConcurrentHashMap<>();


    public StorageManager(Config config) {
        this.config = config;
    }


    public void addIndex(String indexName, IndexSetting indexSetting) throws IOException {
        lock.writeLock().lock();
        try {
            if (indexMap.containsKey(indexName)) {
                return;
            }

            String data = config.getPathConfig().getData();
            String indexDir = data + "/" + indexName;

            IndexMetadata indexMetadata = new IndexMetadata(indexSetting, Path.of(indexDir), indexName);
            indexMap.put(indexName, indexMetadata);
        } finally {
            lock.writeLock().unlock();
        }
    }


    public void removeIndex(String indexName) throws IOException {
        lock.writeLock().lock();
        log.info("[StorageManager] Removing index [{}]", indexName);
        try {
            if (indexMap.containsKey(indexName)) {
                indexMap.get(indexName).close();
                indexMap.remove(indexName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    public IndexSetting getIndexInfo(String indexName) {
        if (!indexMap.containsKey(indexName)) {
            throw new NullPointerException("index not exists");
        }
        return indexMap.get(indexName).getIndexSettings();
    }


    public record DocumentInsertRes(long count, Document document) {
    }

    private void ifAboutCreateIndex(String indexName) throws IOException {
        if(!indexMap.containsKey(indexName)) {
            this.addIndex(indexName,null);
        }
    }

    public void addDocuments(String indexName,List<JSONObject> jsonObject) throws IOException {
        ifAboutCreateIndex(indexName);
        if(Objects.isNull(jsonObject) || jsonObject.isEmpty()) return;
        for (JSONObject object : jsonObject) {
            addDocument(indexName,object);
        }
    }

    public Object addDocument(String indexName, JSONObject jsonObject) throws IOException {
        ifAboutCreateIndex(indexName);
        log.info("[StorageManager] Adding document [{}], json:[{}]", indexName, jsonObject);
        lock.writeLock().lock();
        Document document;
        long count;
        try {
            if (!indexMap.containsKey(indexName) || Objects.isNull(jsonObject) || jsonObject.isEmpty()) {
                throw new NullPointerException("index not exists or add the json-obj is null");
            }

            document = conversionToMap(jsonObject);

            count = indexMap.get(indexName).getIndexWriter().addDocument(document);

        } finally {
            lock.writeLock().unlock();
        }
        return new DocumentInsertRes(count, document);
    }

    private static final String PRIMARY_FIELD_NAME = "id";

    private Document conversionToMap(JSONObject jsonObject) {
        Document document = new Document();
        boolean existsPrimaryField = false;
        Map<String, Object> innerMap = jsonObject.getInnerMap();
        for (Map.Entry<String, Object> entry : innerMap.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            IndexableField field;
            IndexableField nonStoreUse = null;
            if (k.equals(PRIMARY_FIELD_NAME)) {
                existsPrimaryField = true;
                field = new KeywordField(k, (String) v, Field.Store.YES);
            } else {
                switch (v) {
                    case Integer ignored -> {
                        field = new IntPoint(k, ignored);
                        nonStoreUse = new StoredField(k, ignored);
                    }
                    case Long ignored -> {
                        field = new LongPoint(k, ignored);
                        nonStoreUse = new StoredField(k, ignored);
                    }
                    case Double ignored -> {
                        field = new DoublePoint(k, ignored);
                        nonStoreUse = new StoredField(k, ignored);
                    }
                    case Float ignored -> {
                        field = new FloatPoint(k, ignored);
                        nonStoreUse = new StoredField(k, ignored);
                    }
                    case BigDecimal ignored -> {
                        field = new IntPoint(k, ignored.intValue());
                        nonStoreUse = new StoredField(k, ignored.intValue());
                    }
                    case String str -> field = new TextField(k, str, Field.Store.YES);
                    default -> throw new IllegalStateException("Unexpected value: " + v);
                }
            }
            document.add(field);
            if (Objects.nonNull(nonStoreUse)) {
                document.add(nonStoreUse);
            }
        }
        if (!existsPrimaryField) {
            document.add(new KeywordField(PRIMARY_FIELD_NAME, UUID.randomUUID().toString().replaceAll("-", ""), Field.Store.YES));
            throw new NullPointerException("document don`t exists primary key for 'id'.");
        }
        return document;
    }


    private record FetchDocumentResult(int id, Document document, String queryId) {
    }


    private FetchDocumentResult fetchDocumentFromId(String indexName, String documentId) throws Exception {
        ifAboutCreateIndex(indexName);
        lock.readLock().lock();
        Document doc = null;
        int targetDocumentId = -1;
        try {
            if (!indexMap.containsKey(indexName)) throw new NullPointerException("index not exists");

            IndexMetadata indexMetadata = indexMap.get(indexName);

            TermQuery tq = new TermQuery(new Term("id", documentId));
            TopDocs search = indexMetadata.getIndexSearcher().search(tq, 1);

            log.info("[StorageManager] delete document from doc_id [{}], query result count:[{}]", documentId, search.scoreDocs.length);

            if (search.scoreDocs.length > 0) {
                targetDocumentId = search.scoreDocs[0].doc;
                doc = indexMetadata.getIndexSearcher().storedFields().document(targetDocumentId);
            }
        } finally {
            lock.readLock().unlock();
        }

        return new FetchDocumentResult(targetDocumentId, doc, documentId);
    }


    public Document fetchFromDocId(String indexName, String documentId) throws Exception {
        FetchDocumentResult fetchDocumentResult = fetchDocumentFromId(indexName, documentId);
        return fetchDocumentResult.document;
    }


    public Document deleteDocument(String indexName, String documentId) throws Exception {
        this.ifAboutCreateIndex(indexName);
        lock.writeLock().lock();
        Document doc;
        try {
            FetchDocumentResult fetchDocumentResult = fetchDocumentFromId(indexName, documentId);
            doc = fetchDocumentResult.document;
            if (Objects.nonNull(doc)) {
                indexMap.get(indexName).getIndexWriter().deleteDocuments(new Term("id", documentId));
            }
        } finally {
            lock.writeLock().unlock();
        }
        return doc;
    }


    public record QueryResult(List<Document> docs, TotalHits totalHits) {
    }

    ;


    public Object query(String indexName, String json) {
        log.info("[StorageManager] query handler indexName:[{}], queryJson:[{}]", indexName, json);
        if (!indexMap.containsKey(indexName) || Objects.isNull(json) || json.isEmpty()) {
            throw new NullPointerException("index is not exists or query for json-params is empty.");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);

            QueryNode ast = QueryParse.parse(jsonNode);
            if (Objects.isNull(ast)) {
                log.error("[StorageManager] query json-params is empty.]");
                throw new NullPointerException("query json-params is empty.");
            }

            Query luceneQuery = ast.toLuceneQuery();

            IndexSearcher indexSearcher = indexMap.get(indexName).getIndexSearcher();
            TopDocs search = indexSearcher.search(luceneQuery, 10);

            ScoreDoc[] scoreDocs = search.scoreDocs;

            TotalHits totalHits = search.totalHits;
            StoredFields storedFields = indexSearcher.storedFields();
            List<Document> docs = new ArrayList<>();
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document hitDoc = storedFields.document(scoreDoc.doc);
                docs.add(hitDoc);
            }
            return new QueryResult(docs, totalHits);
        } catch (Exception e) {
            log.error("[StorageManager] query exception, query index name [{}] DSL [{}] : {}", indexName, json, e.getMessage(), e);
        }
        return new QueryResult(null, null);
    }

}
