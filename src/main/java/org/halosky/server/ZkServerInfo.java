package org.halosky.server;

import lombok.Data;
import org.halosky.config.Config;
import org.halosky.config.NetworkConfig;
import org.halosky.config.NodeConfig;
import org.halosky.config.TransportTcpConfig;

/**
 * packageName org.halosky.server
 *
 * @author huan.yang
 * @className ZkServerInfo
 * @date 2026/1/13
 */
@Data
public class ZkServerInfo {

    private NodeConfig nodeConfig;

    private NetworkConfig networkConfig;

    private TransportTcpConfig tcpConfig;

    public static ZkServerInfo buildCurrentNodeInfo(Config config) {
        ZkServerInfo zkServerInfo = new ZkServerInfo();
        zkServerInfo.nodeConfig = config.getNodeConfig();
        zkServerInfo.networkConfig = config.getNetworkConfig();
        zkServerInfo.tcpConfig = config.getTcpConfig();
        return zkServerInfo;
    }
}

