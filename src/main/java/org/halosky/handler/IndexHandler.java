package org.halosky.handler;

import lombok.extern.slf4j.Slf4j;
import org.halosky.http.HttpMethodEnum;
import org.halosky.http.RequestContext;
import org.halosky.storage.StorageManager;

import java.io.IOException;
import java.util.Objects;

/**
 * packageName org.halosky.handler
 *
 * @author huan.yang
 * @className IndexHandler
 * @date 2026/1/12
 */
@Slf4j
public class IndexHandler implements AbstractHandler {

    private final StorageManager storageManager;

    public IndexHandler(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    private Object getIndex(String indexName) {
        return storageManager.getIndexInfo(indexName);
    }

    private Object postIndex(String indexName, IndexSetting indexSetting) {
        throw new RuntimeException("post index don`t implement.");
    }

    // create index
    private Object putIndex(String indexName, IndexSetting indexSetting) throws IOException {
        storageManager.addIndex(indexName, indexSetting);
        return indexSetting;
    }

    private Object deleteIndex(String indexName) throws IOException {
        IndexSetting indexInfo = storageManager.getIndexInfo(indexName);
        storageManager.removeIndex(indexName);
        return indexInfo;
    }

    @Override
    public Object handleRequest(RequestContext request) throws Exception {
        log.info("[IndexHandler] processing index request handler. request params :[{}]", request);
        IndexSetting indexSetting = request.conversionBodyToClass(IndexSetting.class);
        String indexName = request.getUri().split("/")[0];

        String name = request.getMethod().name();
        HttpMethodEnum httpMethodEnum = HttpMethodEnum.fromMethod(name);

        if (Objects.isNull(httpMethodEnum)) throw new NullPointerException("http method not exists");

        switch (httpMethodEnum) {
            case GET -> {
                return getIndex(indexName);
            }
            case POST -> {
                return postIndex(indexName, indexSetting);
            }
            case PUT -> {
                return putIndex(indexName, indexSetting);
            }
            case DELETE -> {
                return deleteIndex(indexName);
            }
            default -> throw new Exception("unsupported http method");
        }
    }
}
