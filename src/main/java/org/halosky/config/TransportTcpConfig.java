package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className TransportTcp
 * @date 2026/1/6
 */
@Data
public class TransportTcpConfig {

    private int port;

    private boolean compress;

}
