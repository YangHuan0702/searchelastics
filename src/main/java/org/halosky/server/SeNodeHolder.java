package org.halosky.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.halosky.config.Config;
import org.halosky.config.TransportTcpConfig;
import org.halosky.server.protocol.MessageDecoder;
import org.halosky.server.protocol.MessageEncoder;
import org.halosky.server.protocol.SyncMessage;
import org.halosky.storage.StorageManager;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * packageName org.halosky.server
 *
 * @author huan.yang
 * @className SeNodeHolder
 * @date 2026/1/13
 */
@Slf4j
@Data
public class SeNodeHolder {

    private final String nodeName;

    private final EventLoopGroup eventLoopGroup;

    private final Config config;

    private final ChannelFuture connection;

    private final PingPongCoordinate pingPongCoordinate;

    public SeNodeHolder(String nodeName, ZkServerInfo targetNodeConfigInfo, Config config, StorageManager storageManager) throws InterruptedException {
        this.nodeName = nodeName;
        this.config = config;
        this.pingPongCoordinate = PingPongCoordinate.getInstance();

        String host = targetNodeConfigInfo.getNetworkConfig().getHost();
        TransportTcpConfig tcpConfig = targetNodeConfigInfo.getTcpConfig();

        log.info("[SeNodeHolder] connection to target node [{}] host: [{}] port: [{}]", nodeName, host, tcpConfig.getPort());

        Bootstrap bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4)).addLast(new MessageDecoder()).addLast(new MessageEncoder()).addLast(new NodeSyncHandler(storageManager));

            }
        });
        connection = bootstrap.connect(host, tcpConfig.getPort()).sync();
    }


    public void syncNoRes(SyncMessage message) {
        connection.channel().writeAndFlush(message);
    }


    public String send(SyncMessage message) throws Exception {

        Long requestId = pingPongCoordinate.getRequestId();

        byte[] payload = message.getPayload();
        ByteBuffer wrap = ByteBuffer.allocate(payload.length + 8);
        wrap.put(payload);
        wrap.putLong(requestId);
        message.setPayload(wrap.array());

        SyncMessage syncMessage = pingPongCoordinate.addWatcherRequest(connection.channel(), message, requestId);
        return new String(syncMessage.getPayload(), StandardCharsets.UTF_8);
    }


    public void close() {
        connection.channel().close();
        eventLoopGroup.shutdownGracefully();
    }

}
