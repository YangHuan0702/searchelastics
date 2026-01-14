package org.halosky.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.halosky.config.Config;
import org.halosky.server.protocol.CmdOperator;
import org.halosky.server.protocol.MessageDecoder;
import org.halosky.server.protocol.MessageEncoder;
import org.halosky.server.protocol.SyncMessage;
import org.halosky.storage.StorageManager;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * packageName org.halosky.server
 *
 * @author huan.yang
 * @className NodeServer
 * @date 2026/1/13
 */
@Slf4j
public class NodeServer {

    private final Map<String, SeNodeHolder> clients = new HashMap<>();

    private final StorageManager storageManager;
    private final ZkServerManager zkServerManager;
    private final Config config;

    private final EventLoopGroup boos;
    private final EventLoopGroup work;
    private final Channel channel;

    public NodeServer(StorageManager storageManager, ZkServerManager zkServerManager, Config config) throws InterruptedException {
        this.storageManager = storageManager;
        this.zkServerManager = zkServerManager;
        this.config = config;

        ServerBootstrap bootstrap = new ServerBootstrap();
        boos = new NioEventLoopGroup();
        work = new NioEventLoopGroup();

        bootstrap.group(boos, work).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4)).addLast(new MessageDecoder()).addLast(new MessageEncoder()).addLast(new NodeSyncHandler(storageManager));
            }
        });

        channel = bootstrap.bind(config.getNetworkConfig().getHost(), config.getTcpConfig().getPort()).sync().channel();
        log.info("[NodeServer] node server started, bind host [{}] and port [{}].", config.getNetworkConfig().getHost(), config.getTcpConfig().getPort());
    }


    private void connectionToTargetNode(String targetNodeName) throws Exception {
        if (!clients.containsKey(targetNodeName)) {
            String clusterNode = zkServerManager.getClusterNode(targetNodeName);
            ZkServerInfo zkServerInfo = JSON.parseObject(clusterNode, ZkServerInfo.class);
            clients.put(targetNodeName, new SeNodeHolder(targetNodeName, zkServerInfo, config, storageManager));
        }
    }

    public synchronized void syncDelDocument(String nodeName, String indexName, String docId) throws Exception {
        log.info("[NodeServer] sync del-documents to target node [{}], indexName: [{}], docId: [{}]", nodeName, indexName, docId);
        connectionToTargetNode(nodeName);
        SeNodeHolder seNodeHolder = clients.get(nodeName);

        SyncMessage message = new SyncMessage();
        message.setCmd(CmdOperator.DEL_DOCUMENT.getCode());

        byte[] indexBytes = indexName.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = docId.getBytes(StandardCharsets.UTF_8);

        ByteBuffer allocate = ByteBuffer.allocate(8 + indexBytes.length + dataBytes.length);
        allocate.putInt(indexBytes.length);
        allocate.put(indexBytes);
        allocate.putInt(dataBytes.length);
        allocate.put(dataBytes);
        message.setPayload(allocate.array());

        seNodeHolder.syncNoRes(message);
    }


    public synchronized void syncNewDocuments(String nodeName, String indexName, List<JSONObject> value) throws Exception {
        log.info("[NodeServer] sync new-documents to target node [{}], indexName: [{}], count: [{}]", nodeName, indexName, value.size());
        connectionToTargetNode(nodeName);
        SeNodeHolder seNodeHolder = clients.get(nodeName);

        SyncMessage message = new SyncMessage();
        message.setCmd(CmdOperator.ADD_DOCUMENT.getCode());

        int indexLength = indexName.length();
        byte[] indexBytes = indexName.getBytes(StandardCharsets.UTF_8);

        String jsonString = JSON.toJSONString(value);
        byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
        int payloadLength = bytes.length;

        ByteBuffer allocate = ByteBuffer.allocate(8 + indexBytes.length + payloadLength);
        allocate.putInt(indexLength);
        allocate.put(indexBytes);
        allocate.putInt(payloadLength);
        allocate.put(bytes);

        message.setPayload(allocate.array());
        seNodeHolder.syncNoRes(message);
    }


    public void close() {
        channel.close();
        boos.shutdownGracefully();
        work.shutdownGracefully();
    }

    public String syncQueryDocument(String indexName, String dsl,String nodeName) throws Exception {
        log.info("[NodeServer] sync query-documents to target node [{}], indexName: [{}], dsl: [{}]", nodeName, indexName, dsl);
        connectionToTargetNode(nodeName);

        SeNodeHolder seNodeHolder = clients.get(nodeName);

        SyncMessage message = new SyncMessage();
        message.setCmd(CmdOperator.FET_DOCUMENT.getCode());

        int indexLength = indexName.length();
        byte[] indexBytes = indexName.getBytes(StandardCharsets.UTF_8);

        byte[] bytes = dsl.getBytes(StandardCharsets.UTF_8);
        int payloadLength = bytes.length;

        ByteBuffer allocate = ByteBuffer.allocate(8 + indexBytes.length + payloadLength);
        allocate.putInt(indexLength);
        allocate.put(indexBytes);
        allocate.putInt(payloadLength);
        allocate.put(bytes);

        message.setPayload(allocate.array());

        String send = seNodeHolder.send(message);
        log.info("[NodeServer] remo-call document query for index: [{}],dsl: [{}],res: [{}]",indexName,dsl,send);
        return send;
    }


}
