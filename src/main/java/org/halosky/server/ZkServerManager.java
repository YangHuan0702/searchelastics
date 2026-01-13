package org.halosky.server;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.halosky.config.Config;
import org.halosky.config.NodeConfig;
import org.halosky.config.ZkConfig;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * packageName org.halosky.server
 *
 * @author huan.yang
 * @className ZkServerManager
 * @date 2026/1/13
 */
@Slf4j
public class ZkServerManager {

    private final CuratorFramework zkClient;

    private final Config config;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public ZkServerManager(Config config) throws Exception {
        this.config = config;
        ZkConfig zkConfig = config.getZkConfig();

        RetryPolicy retryPolicy = new RetryNTimes(zkConfig.getRetryNum(), zkConfig.getRetryDelayMs());
        zkClient = CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(zkConfig.getConnectionTimeout() * 1000)
                .sessionTimeoutMs(zkConfig.getSessionTimeout() * 1000)
                .connectString(zkConfig.getUrl())
                .retryPolicy(retryPolicy)
                .namespace(zkConfig.getNamespace())
                .build();

        start();

        registry();
    }

    public void start() throws Exception {
        log.info("[ZkServerManager] start zk-client.");
        zkClient.start();
        running.compareAndSet(false, true);

        // registry cluster node
        String clusterPath = "/" + config.getClusterConfig().getName();

        this.checkAndCreateDir(clusterPath, CreateMode.PERSISTENT);
    }


    public void checkAndCreateDataNode(String path, CreateMode mode, byte[] data) throws Exception {
        if (!running.get()) {
            throw new RuntimeException("[ZkServerManager] zk-client is not start.");
        }
        Stat stat = zkClient.checkExists().forPath(path);
        if (Objects.isNull(stat)) {
            zkClient.create().withMode(mode).forPath(path, data);
        }
    }

    public void checkAndCreateDir(String path, CreateMode mode) throws Exception {
        if (!running.get()) {
            throw new RuntimeException("[ZkServerManager] zk-client is not start.");
        }

        Stat stat = zkClient.checkExists().forPath(path);

        if (Objects.isNull(stat)) {
            zkClient.create().withMode(mode).forPath(path);
        }
    }


    private void registry() throws Exception {
        if (!running.get()) {
            throw new RuntimeException("[ZkServerManager] zk-client is not start.");
        }
        log.info("[ZkServerManager] start registry current node info to zk-server.");
        NodeConfig nodeConfig = config.getNodeConfig();
        String name = nodeConfig.getName();

        String nodePath = "/" + config.getClusterConfig().getName() + "/" + name;
        ZkServerInfo zkServerInfo = ZkServerInfo.buildCurrentNodeInfo(config);
        checkAndCreateDataNode(nodePath, CreateMode.EPHEMERAL, JSON.toJSONBytes(zkServerInfo));
    }

    public ClusterNodeInfo clusterNodes() throws Exception {
        return this.getChildren("/"+config.getClusterConfig().getName());
    }


    public static record ClusterNodeInfo(Map<String,String> nodeInfoMap,List<String> nodes) {}


    public ClusterNodeInfo getChildren(String path) throws Exception {
        if (!running.get()) {
            throw new RuntimeException("[ZkServerManager] zk-client is not start.");
        }
        List<String> strings = zkClient.getChildren().forPath(path);

        Map<String,String> ans = new HashMap<>();

        if(Objects.nonNull(strings) && !strings.isEmpty()) {
            for (String string : strings) {
                String dataPath = path + "/" + string;
                byte[] bytes = zkClient.getData().forPath(dataPath);
                ans.put(string,new String(bytes, StandardCharsets.UTF_8));
            }
        }

        return new ClusterNodeInfo(ans,strings);
    }


    public String getClusterNode(String nodeName) throws Exception {
        if (!running.get()) {
            throw new RuntimeException("[ZkServerManager] zk-client is not start.");
        }
        log.info("[ZkServerManager] get target node [{}] of info config.",nodeName);
        ClusterNodeInfo children = getChildren(nodeName);

        for(Map.Entry<String, String> entry : children.nodeInfoMap.entrySet()) {
            if(entry.getKey().equals(nodeName)) return entry.getValue();
        }
        return null;
    }


    public void close() {
        zkClient.close();
        running.compareAndSet(true, false);
    }

}
