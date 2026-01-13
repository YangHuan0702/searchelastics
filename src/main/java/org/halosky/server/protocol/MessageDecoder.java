package org.halosky.server.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * packageName org.halosky.server.protocol
 *
 * @author huan.yang
 * @className MessageDecoder
 * @date 2026/1/13
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int operator = in.readInt();
        CmdOperator cmdOperator = CmdOperator.getByCode(operator);
        if(cmdOperator == null) {
            throw new NullPointerException("i don`t fucking know the operator :" + operator);
        }
        log.info("[MessageDecoder] reply server operator [{}]:[{}]",operator,cmdOperator.getMsg());
        int length = in.readInt();

        byte[] payload = new byte[length];
        in.readBytes(payload);

        SyncMessage syncMessage = new SyncMessage();
        syncMessage.setCmd(operator);
        syncMessage.setPayload(payload);
        out.add(syncMessage);
    }
}
