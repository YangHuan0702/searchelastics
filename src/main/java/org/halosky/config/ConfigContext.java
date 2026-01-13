package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className ConfigContext
 * @date 2026/1/6
 */
@Data
public class ConfigContext {

    private ClusterConfig cluster;
    private GatewayConfig gateway;
    private HttpConfig http;
    private IndexConfig index;
    private NetworkConfig network;
    private NodeConfig node;
    private PathConfig path;
    private RecoveryConfig recovery;
    private TransportTcpConfig tcp;
    private ZkConfig zk;

}
