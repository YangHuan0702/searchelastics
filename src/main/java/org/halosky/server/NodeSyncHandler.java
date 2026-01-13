package org.halosky.server;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.halosky.server.protocol.CmdOperator;
import org.halosky.server.protocol.SyncMessage;
import org.halosky.storage.StorageManager;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * packageName org.halosky.server
 *
 * @author huan.yang
 * @className EsNode
 * @date 2026/1/6
 */
@Slf4j
public class NodeSyncHandler extends SimpleChannelInboundHandler<SyncMessage> {

    private final StorageManager storageManager;

    public NodeSyncHandler(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SyncMessage msg) throws Exception {
        int cmd = msg.getCmd();
        log.info("[SeNode]processing server sync operator: [{}]", cmd);

        CmdOperator operator = CmdOperator.getByCode(cmd);
        assert operator != null;
        switch (operator) {
            case ADD_INDEX, DEL_INDEX -> indexHandler(operator, new String(msg.getPayload(), StandardCharsets.UTF_8));
            case ADD_DOCUMENT, DEL_DOCUMENT, UPD_DOCUMENT, FET_DOCUMENT -> documentHandler(operator, msg.getPayload());
            default -> throw new Exception("Unknown operator");
        }
    }

    private void documentHandler(CmdOperator operator, byte[] payload) throws Exception {
        ByteBuffer wrap = ByteBuffer.wrap(payload);
        wrap.flip();
        int nextLength = wrap.getInt();
        byte[] data = new byte[nextLength];
        ByteBuffer byteBuffer = wrap.get(data);
        String indexName = new String(byteBuffer.array(), StandardCharsets.UTF_8);

        nextLength = wrap.getInt();
        data = new byte[nextLength];
        ByteBuffer byteBuffer1 = wrap.get(data);
        String payloadData = new String(byteBuffer1.array(), StandardCharsets.UTF_8);

        switch (operator) {
            case ADD_DOCUMENT -> {
                List<JSONObject> objects = JSON.parseArray(payloadData, com.alibaba.fastjson.JSONObject.class);
                storageManager.addDocuments(indexName,objects);
            }
            case DEL_DOCUMENT -> storageManager.deleteDocument(indexName,payloadData);
            case FET_DOCUMENT -> storageManager.query(indexName,payloadData);
            default -> throw new Exception("Unknown document operator");
        }
    }

    private void indexHandler(CmdOperator operator, String indexName) throws Exception {
        switch (operator) {
            case ADD_INDEX -> storageManager.addIndex(indexName, null);
            case DEL_INDEX -> storageManager.removeIndex(indexName);
            default -> throw new IllegalArgumentException("unknow index operator.");
        }
    }
}
