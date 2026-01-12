package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className HttpConfig
 * @date 2026/1/6
 */
@Data
public class HttpConfig {

    private int port;

    private int maxContentLengthMb;

    private boolean enabled;

}
