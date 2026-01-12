package org.halosky.storage;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.halosky.config.ConfigContext;
import org.halosky.handler.IndexSetting;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

    private final ConfigContext config;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, IndexMetadata> indexMap = new ConcurrentHashMap<>();


    public StorageManager(ConfigContext config) {
        this.config = config;
    }


    public void addIndex(String indexName, IndexSetting indexSetting) throws IOException {
        lock.writeLock().lock();
        try {
            if (indexMap.containsKey(indexName)) {
                throw new RuntimeException("index already exists");
            }

            String data = config.getPath().getData();
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

    public Object addDocument(String indexName, JSONObject jsonObject) throws IOException {
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
        }
        return document;
    }


    private record FetchDocumentResult(int id, Document document, String queryId) {
    }


    private FetchDocumentResult fetchDocumentFromId(String indexName, String documentId) throws Exception {
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




    public Object query(String indexName, JSONObject json) {
        log.info("[StorageManager] query handler indexName:[{}], queryJson:[{}]",indexName,json);
        if(!indexMap.containsKey(indexName) || Objects.isNull(json) || json.isEmpty()) {
            throw new NullPointerException("index is not exists or query for json-params is empty.");
        }


        return null;
    }

}
