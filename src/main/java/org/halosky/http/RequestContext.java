package org.halosky.http;

import com.alibaba.fastjson.JSON;
import com.twelvemonkeys.lang.StringUtil;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * packageName org.halosky.http
 *
 * @author huan.yang
 * @className RequestContext
 * @date 2026/1/12
 */
@Data
@AllArgsConstructor
@ToString
public class RequestContext {

    private Map<String, List<String>> pathParams;

    private HttpMethod method;

    private String requestBody;

    private String uri;


    public <T> T conversionBodyToClass(Class<T> tClass) {
        if(StringUtil.isEmpty(requestBody)) return null;
        return JSON.parseObject(requestBody, tClass);
    }


    public static RequestContext build(Map<String, List<String>> pathParams, HttpMethod method, String requestBody,String uri) {
        return new  RequestContext(pathParams, method, requestBody, uri);
    }

}
