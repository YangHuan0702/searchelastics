package org.halosky.shard;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.twelvemonkeys.lang.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.halosky.config.Config;
import org.halosky.server.NodeServer;
import org.halosky.server.ZkServerManager;
import org.halosky.storage.StorageManager;

import java.util.*;

/**
 * packageName org.halosky.shard
 *
 * @author huan.yang
 * @className ShardManager
 * @date 2026/1/12
 */
@Slf4j
@Data
public class ShardManager {

    private final RoutingManager routingManager;

    private final ZkServerManager zkServerManager;

    private final StorageManager storageManager;

    private final Config config;

    private final NodeServer nodeServer;

    public ShardManager(RoutingManager routingManager, ZkServerManager zkServerManager, Config config,NodeServer nodeServer) {
        this.routingManager = routingManager;
        this.zkServerManager = zkServerManager;
        this.storageManager = new StorageManager(config);
        this.config = config;

        this.nodeServer = nodeServer;
    }


    private Map<String,List<JSONObject>> allocation(ZkServerManager.ClusterNodeInfo clusterNodeInfo,List<JSONObject> docs) {
        List<String> array = clusterNodeInfo.nodes();
        Map<String,List<JSONObject>> shardMap = new HashMap<>();

        for (JSONObject doc : docs) {
            String s = doc.getString("id");
            // do you not like this way, you can use Collections.shuffle() to node array.
            int routing = routingManager.routing(s);

            String nodeName = array.get(routing);
            List<JSONObject> documents = shardMap.computeIfAbsent(nodeName, v -> new ArrayList<>());
            documents.add(doc);
        }
        return shardMap;
    }

    public void shardingDelDocument(String indexName,String docId) throws Exception{
        log.info("[ShardManager] sharding delete target doc. indexName: [{}], docId: [{}]",indexName,docId);

        ZkServerManager.ClusterNodeInfo clusterNodes = zkServerManager.clusterNodes();
        if(config.getIndexConfig().getNumberOfShards() > clusterNodes.nodes().size()) {
            throw new IllegalArgumentException("shard of number ["+config.getIndexConfig().getNumberOfShards()+"] current cluster node size ["+clusterNodes.nodes().size()+"]");
        }

        int routing = routingManager.routing(docId);
        String targetNodeName = clusterNodes.nodes().get(routing);

        if(targetNodeName.equals(config.getNodeConfig().getName())) {
            storageManager.deleteDocument(indexName,docId);
        } else {
            nodeServer.syncDelDocument(targetNodeName,indexName,docId);
        }
    }


    public void shardingAddDocument(List<JSONObject> documents,String indexName) throws Exception {
        log.info("[ShardManager] sharding target documents [{}] start.",documents.size());
        ZkServerManager.ClusterNodeInfo clusterNodes = this.zkServerManager.clusterNodes();
        if(config.getIndexConfig().getNumberOfShards() > clusterNodes.nodes().size()) {
            throw new IllegalArgumentException("shard of number ["+config.getIndexConfig().getNumberOfShards()+"] current cluster node size ["+clusterNodes.nodes().size()+"]");
        }

        Map<String, List<JSONObject>> allocation = allocation(clusterNodes, documents);

        for(Map.Entry<String,List<JSONObject>> entry : allocation.entrySet()) {
            // current node
            if(entry.getKey().equals(config.getNodeConfig().getName())) {
                storageManager.addDocuments(indexName,entry.getValue());
            } else {
                // send operator to target entry.getKey() node.
                nodeServer.syncNewDocuments(entry.getKey(),indexName,entry.getValue());
            }
        }
    }



    public List<JSONObject> shardingQuery(String indexName,String dsl) throws Exception {
        log.info("[ShardManager] start join sharding-query index name: [{}] dsl: [{}]",indexName,dsl);

        ZkServerManager.ClusterNodeInfo clusterNodes = this.zkServerManager.clusterNodes();
        if(config.getIndexConfig().getNumberOfShards() > clusterNodes.nodes().size()) {
            throw new IllegalArgumentException("shard of number ["+config.getIndexConfig().getNumberOfShards()+"] current cluster node size ["+clusterNodes.nodes().size()+"]");
        }

        List<String> strings = clusterNodes.nodes().subList(0, config.getIndexConfig().getNumberOfShards());

        List<JSONObject> ans = new ArrayList<>();

        for (String nodeName : strings) {
            if (nodeName.equals(config.getNodeConfig().getName())) {
                // current node
                StorageManager.QueryResult query = (StorageManager.QueryResult)storageManager.query(indexName, dsl);
                if(Objects.nonNull(query.docs()) && !query.docs().isEmpty()) {
                    ans.addAll(query.docs());
                }
            } else {
                String s = nodeServer.syncQueryDocument(indexName, dsl, nodeName);
                if(!StringUtil.isEmpty(s)) {
                    List<JSONObject> documents = JSON.parseArray(s, JSONObject.class);
                    ans.addAll(documents);
                }
            }
        }
        return ans;
    }




}
