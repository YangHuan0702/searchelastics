package org.halosky.handler;

import org.halosky.http.RequestContext;

/**
 * packageName org.halosky.handler
 *
 * @author huan.yang
 * @className AbstractHandler
 * @date 2026/1/12
 */
public interface AbstractHandler {

    Object handleRequest(RequestContext request) throws Exception;

}
