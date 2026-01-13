package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className ZkConfig
 * @date 2026/1/13
 */
@Data
public class ZkConfig {

    private String url;

    private Integer sessionTimeout;

    private Integer connectionTimeout;

    private String namespace;

    private Integer retryNum;

    private Integer retryDelayMs;

}
