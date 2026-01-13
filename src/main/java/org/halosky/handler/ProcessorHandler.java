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

    public ProcessorHandler(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public record HttpResp(int code, String msg){};

    @Override
    public Object handleRequest(RequestContext requestContext) throws Exception {
        String uri = requestContext.getUri();
        String[] resources = null;
        if(StringUtil.isEmpty(uri) || Objects.isNull((resources = uri.split("/"))) || resources.length < 2) {
            throw new NullPointerException("[ProcessorHandler] unknow uri mapping operator.");
        }

//        return resources.length == 1 ? indexHandler.handleRequest(requestContext) : documentHandler.handleRequest(requestContext);

        String indexName = resources[0];

        String name = requestContext.getMethod().name();
        HttpMethodEnum httpMethodEnum = HttpMethodEnum.fromMethod(name);
        if(Objects.isNull(httpMethodEnum)) throw new NullPointerException("i don`t fucking know http method of process.");


        switch (httpMethodEnum) {
            case PUT -> {
                String context = requestContext.getRequestBody();
                List<JSONObject> jsonObjects = JSON.parseArray(context, JSONObject.class);
                shardManager.shardingAddDocument(jsonObjects,indexName);
            }
            case DELETE -> {
                String docId = resources[1];
                shardManager.shardingDelDocument(indexName,docId);
            }
        }
        return new HttpResp(200,"operator success.");

    }
}
