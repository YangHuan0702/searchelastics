package org.halosky.http;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.halosky.handler.ProcessorHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * packageName org.halosky.http
 *
 * @author huan.yang
 * @className HttpHandler
 * @date 2026/1/7
 */
@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ProcessorHandler processorHandler;

    public HttpHandler(ProcessorHandler processorHandler) {
        this.processorHandler = processorHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        HttpMethod method = request.method();
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, List<String>> params = decoder.parameters();
        ByteBuf content = request.content();
        String s = content.toString(StandardCharsets.UTF_8);

        Object resp = processorHandler.handleRequest(RequestContext.build(params, method, s, uri));

        FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(resp).getBytes()));
        res.headers().set("Content-Type", "application/json");
        res.headers().set("Content-Length", res.content().readableBytes());
        ctx.writeAndFlush(res);
    }
}
