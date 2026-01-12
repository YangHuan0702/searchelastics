package org.halosky.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.halosky.config.HttpConfig;
import org.halosky.config.NetworkConfig;
import org.halosky.handler.ProcessorHandler;

/**
 * packageName org.halosky.http
 *
 * @author huan.yang
 * @className HttpServer
 * @date 2026/1/6
 */
@Slf4j
public class HttpServer {


    private final HttpConfig httpConfig;

    private final NetworkConfig networkConfig;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final HttpHandler httpHandler;


    public HttpServer(HttpConfig httpConfig,NetworkConfig networkConfig) {
        log.info("[HttpServer] http server is starting, config: [{}]", httpConfig);
        this.httpConfig = httpConfig;
        this.networkConfig = networkConfig;

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ProcessorHandler processorHandler = new ProcessorHandler();
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
            ChannelFuture sync = serverBootstrap.bind(networkConfig.getHost(),httpConfig.getPort()).sync();
            log.info("[HttpServer] http server started on host [{}] and port [{}]", networkConfig.getHost(),httpConfig.getPort());
            sync.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("[HttpServer] http server start error {}",e.getMessage(),e);
        }

    }


    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


}
