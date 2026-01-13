package org.halosky;

import lombok.extern.slf4j.Slf4j;
import org.halosky.config.Config;
import org.halosky.http.HttpServer;
import org.halosky.server.NodeServer;
import org.halosky.server.ZkServerManager;
import org.halosky.shard.RoutingManager;
import org.halosky.shard.ShardManager;
import org.halosky.storage.StorageManager;

/**
 * packageName org.halosky
 *
 * @author huan.yang
 * @className SEServer
 * @date 2026/1/13
 */
@Slf4j
public class SEServer {

    private final Config config;

    private final ZkServerManager zkServerManager;

    private final NodeServer nodeServer;
    private final HttpServer httpServer;

    public SEServer(Config config)throws Exception {
        this.config = config;

        zkServerManager = new ZkServerManager(config);

        RoutingManager routingManager = new RoutingManager(config.getIndexConfig());
        StorageManager storageManager = new StorageManager(config);

        nodeServer = new NodeServer(storageManager,zkServerManager,config);
        ShardManager shardManager = new ShardManager(routingManager, zkServerManager, config, nodeServer);

        httpServer = new HttpServer(config, shardManager);
    }


    public void close(){
        httpServer.close();
        nodeServer.close();
        zkServerManager.close();
    }


}
