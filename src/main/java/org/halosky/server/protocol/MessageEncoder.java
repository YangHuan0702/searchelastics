package org.halosky.server.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

/**
 * packageName org.halosky.server.protocol
 *
 * @author huan.yang
 * @className MessageEncoder
 * @date 2026/1/13
 */
public class MessageEncoder extends MessageToByteEncoder<SyncMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, SyncMessage msg, ByteBuf out) throws Exception {
        int cmd = msg.getCmd();
        CmdOperator operator = CmdOperator.getByCode(cmd);
        if(Objects.isNull(operator)) throw new NullPointerException("i don`t fucking know the operator.");

        int length = 8 + msg.getPayload().length;

        out.writeInt(length);
        out.writeInt(cmd);
        out.writeInt(msg.getPayload().length);
        out.writeBytes(msg.getPayload());
    }
}
