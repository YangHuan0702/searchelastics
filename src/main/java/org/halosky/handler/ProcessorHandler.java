package org.halosky.handler;

import com.twelvemonkeys.lang.StringUtil;
import org.halosky.http.RequestContext;

import java.util.Objects;

/**
 * packageName org.halosky.handler
 *
 * @author huan.yang
 * @className ProcessorHandler
 * @date 2026/1/12
 */
public class ProcessorHandler implements AbstractHandler {

    private final IndexHandler indexHandler;
    private final DocumentHandler documentHandler;

    public ProcessorHandler(IndexHandler indexHandler, DocumentHandler documentHandler) {
        this.indexHandler = indexHandler;
        this.documentHandler = documentHandler;
    }

    @Override
    public Object handleRequest(RequestContext requestContext) throws Exception {
        String uri = requestContext.getUri();
        String[] resources = null;
        if(StringUtil.isEmpty(uri) || Objects.isNull((resources = uri.split("/")))) {
            throw new NullPointerException("[ProcessorHandler] unknow uri mapping operator.");
        }

        return resources.length == 1 ? indexHandler.handleRequest(requestContext) : documentHandler.handleRequest(requestContext);
    }
}
