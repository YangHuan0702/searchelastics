package org.halosky.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.halosky.http.HttpMethodEnum;
import org.halosky.http.RequestContext;
import org.halosky.storage.StorageManager;

import java.io.IOException;
import java.util.Objects;

/**
 * packageName org.halosky.handler
 *
 * @author huan.yang
 * @className DocumentHandler
 * @date 2026/1/12
 */
@Slf4j
public class DocumentHandler implements AbstractHandler {

    private final static String DOCUMENT_CONST = "_doc";

    private final StorageManager storageManager;

    // check handled doc.
//    private byte[] bitMap;


    public DocumentHandler(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @Override
    public Object handleRequest(RequestContext request) throws Exception {
        log.info("[DocumentHandler] processing document request,request params :[{}]",request);

        HttpMethodEnum httpMethodEnum = HttpMethodEnum.fromMethod(request.getMethod().name());
        if(Objects.isNull(httpMethodEnum)){ throw new NullPointerException("http method not exists");}

        String uri = request.getUri();
        String[] objects = uri.split("/");


        if((httpMethodEnum == HttpMethodEnum.DELETE || httpMethodEnum == HttpMethodEnum.GET) && objects.length != 3) {
            throw new IllegalArgumentException("'[DocumentHandler] delete document error, illegal uri");
        } else if(objects.length != 2) {
            throw new IllegalArgumentException("'[DocumentHandler] delete document error, illegal uri");
        }

        String indexName = objects[0];
        String documentConst = objects[1];
        if(!documentConst.equals(DOCUMENT_CONST)) throw new IllegalArgumentException("'[DocumentHandler] document handler error,two params don`t _doc for split uri after");

        switch (httpMethodEnum) {
            case DELETE -> {
                String documentId = objects[2];
                return deleteDocument(indexName,documentId);
            }
            case POST -> {
                return queryDocument(indexName,request.getRequestBody());
            }
            case PUT -> {
                return createDocument(indexName,request.getRequestBody());
            }
            case GET -> {
                String documentId = objects[2];
                return getDocumentById(indexName,documentId);
            }
            default -> throw new IllegalArgumentException("'[DocumentHandler] delete document error, illegal uri");
        }
    }

    private Document getDocumentById(String indexName, String docId) throws Exception {
        return storageManager.fetchFromDocId(indexName,docId);
    }

    private Object createDocument(String indexName,String documentJson) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(documentJson);

        return storageManager.addDocument(indexName,jsonObject);
    }

    private Object queryDocument(String indexName, String queryJsonString) throws Exception {
        log.info("[DocumentHandler] index-query processing. query index name [{}], query-json DSL is [{}]",indexName,queryJsonString);
        return storageManager.query(indexName,queryJsonString);
    }

    private Object deleteDocument(String indexName,String docId) throws Exception {
        return storageManager.deleteDocument(indexName,docId);
    }
}
