package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className GatewayConfig
 * @date 2026/1/6
 */
@Data
public class GatewayConfig {

    private String type;

    private int recoverAfterNodes;

    private int recoverAfterTime;

    private int expectedNodes;

}
