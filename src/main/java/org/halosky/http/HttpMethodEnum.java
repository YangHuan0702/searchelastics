package org.halosky.http;

import lombok.Getter;

/**
 * packageName org.halosky.http
 *
 * @author huan.yang
 * @className HttpMethodEnum
 * @date 2026/1/12
 */
@Getter
public enum HttpMethodEnum {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String method;

    HttpMethodEnum(String method) {
        this.method = method;
    }

    public static HttpMethodEnum fromMethod(String method) {
        for (HttpMethodEnum value : values()) {
            if(value.getMethod().equals(method))
                return value;
        }
        return null;
    }

}
