package org.halosky.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.halosky.config.Config;
import org.halosky.handler.ProcessorHandler;
import org.halosky.server.ZkServerManager;
import org.halosky.shard.ShardManager;

/**
 * packageName org.halosky.http
 *
 * @author huan.yang
 * @className HttpServer
 * @date 2026/1/6
 */
@Slf4j
public class HttpServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final HttpHandler httpHandler;
    private final Config config;
    private final ShardManager shardManager;

    private final ZkServerManager zkServerManager;

    public HttpServer(Config config,ShardManager shardManager) throws Exception {
        log.info("[HttpServer] http server is starting, config: [{}]", config);
        this.config = config;
        this.zkServerManager = new ZkServerManager(config);

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        this.shardManager = shardManager;
        ProcessorHandler processorHandler = new ProcessorHandler(this.shardManager);
        httpHandler = new HttpHandler(processorHandler);
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        ServerBootstrap serverBootstrap = bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(httpHandler);
                    }
                });
        try {
            ChannelFuture sync = serverBootstrap.bind(config.getNetworkConfig().getHost(),config.getHttpConfig().getPort()).sync();
            log.info("[HttpServer] http server started on host [{}] and port [{}]", config.getNetworkConfig().getHost(),config.getHttpConfig().getPort());
            sync.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("[HttpServer] http server start error {}",e.getMessage(),e);
        }

    }


    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


}
