package org.halosky.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.twelvemonkeys.lang.StringUtil;
import org.halosky.http.HttpMethodEnum;
import org.halosky.http.RequestContext;
import org.halosky.shard.ShardManager;

import java.util.List;
import java.util.Objects;

/**
 * packageName org.halosky.handler
 *
 * @author huan.yang
 * @className ProcessorHandler
 * @date 2026/1/12
 */
public class ProcessorHandler implements AbstractHandler {

    private final ShardManager shardManager;

    private final static String DOCS_TIPS = "docs";
    private final static String PATH_DOCS_TIPS = "_docs";

    public ProcessorHandler(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public record HttpResp(int code, String msg, Object data) {
    }

    @Override
    public Object handleRequest(RequestContext requestContext) throws Exception {
        String uri = requestContext.getUri();
        String[] resources = null;
        if (StringUtil.isEmpty(uri) || Objects.isNull((resources = uri.split("/"))) || resources.length < 3) {
            throw new NullPointerException("[ProcessorHandler] unknow uri mapping operator.");
        }

//        return resources.length == 1 ? indexHandler.handleRequest(requestContext) : documentHandler.handleRequest(requestContext);

        String indexName = resources[1];

        if (resources[1].equals(PATH_DOCS_TIPS)) {
            throw new IllegalArgumentException("please use /*/_docs way to go to Operator.");
        }

        String name = requestContext.getMethod().name();
        HttpMethodEnum httpMethodEnum = HttpMethodEnum.fromMethod(name);
        if (Objects.isNull(httpMethodEnum))
            throw new NullPointerException("i don`t fucking know http method of process.");


        Object data = null;
        switch (httpMethodEnum) {
            case PUT -> {
                String context = requestContext.getRequestBody();
                JSONObject jsonObject = JSON.parseObject(context);
                if (!jsonObject.containsKey(DOCS_TIPS) || StringUtil.isEmpty(jsonObject.getString(DOCS_TIPS))) {
                    throw new NullPointerException("[ProcessorHandler] request body don`t exist `docs` or `docs` is empty.");
                }
                String docs = jsonObject.getString(DOCS_TIPS);
                List<JSONObject> jsonObjects = JSON.parseArray(docs, JSONObject.class);
                shardManager.shardingAddDocument(jsonObjects, indexName);
            }
            case DELETE -> {
                String docId = resources[3];
                shardManager.shardingDelDocument(indexName, docId);
            }
            case POST -> {
                String context = requestContext.getRequestBody();
                data = shardManager.shardingQuery(indexName, context);
            }
        }
        return new HttpResp(200, "operator success.", data);

    }
}
