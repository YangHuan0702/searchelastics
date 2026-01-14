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

    private final PingPongCoordinate pingpongCoordinate;

    public NodeSyncHandler(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.pingpongCoordinate = PingPongCoordinate.getInstance();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SyncMessage msg) throws Exception {
        int cmd = msg.getCmd();
        log.info("[SeNode]processing server sync operator: [{}]", cmd);

        CmdOperator operator = CmdOperator.getByCode(cmd);
        assert operator != null;
        switch (operator) {
            case ADD_INDEX, DEL_INDEX -> indexHandler(operator, new String(msg.getPayload(), StandardCharsets.UTF_8));
            case ADD_DOCUMENT, DEL_DOCUMENT, UPD_DOCUMENT -> documentHandler(operator, msg.getPayload());
            case FET_DOCUMENT -> {
                SyncMessage res = queryDocument(msg.getPayload());
                ctx.writeAndFlush(res);
            }
            case RES_DOCS -> {
                ByteBuffer wrap = ByteBuffer.wrap(msg.getPayload());
                long requestId = wrap.getLong();
                int resLength = wrap.getInt();

                byte[] json = new byte[resLength];
                wrap.get(json);

                SyncMessage syncMessage = new SyncMessage();
                syncMessage.setCmd(CmdOperator.RES_DOCS.getCode());
                syncMessage.setPayload(json);
                pingpongCoordinate.notifyWaitRequest(requestId,syncMessage);
            }
            default -> throw new Exception("Unknown operator");
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[NodeSyncHandler] node-server error.",cause);
    }

    private SyncMessage queryDocument(byte[] payload) throws Exception {
        ByteBuffer wrap = ByteBuffer.wrap(payload);
        int nextLength = wrap.getInt();
        byte[] data = new byte[nextLength];
        wrap.get(data);
        String indexName = new String(data, StandardCharsets.UTF_8);

        nextLength = wrap.getInt();
        data = new byte[nextLength];
        wrap.get(data);
        String dsl = new String(data, StandardCharsets.UTF_8);

        long requestId = wrap.getLong();
        StorageManager.QueryResult query = (StorageManager.QueryResult)storageManager.query(indexName, dsl);

        List<JSONObject> docs = query.docs();
        SyncMessage res = new SyncMessage();
        res.setCmd(CmdOperator.RES_DOCS.getCode());
        String jsonString = JSON.toJSONString(docs);
        byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);

        ByteBuffer allocate = ByteBuffer.allocate(bytes.length + 8 + 4);
        allocate.putLong(requestId);
        allocate.putInt(bytes.length);
        allocate.put(bytes);
        res.setPayload(allocate.array());
        return res;
    }

    private void documentHandler(CmdOperator operator, byte[] payload) throws Exception {
        ByteBuffer wrap = ByteBuffer.wrap(payload);
        int nextLength = wrap.getInt();
        byte[] data = new byte[nextLength];
        wrap.get(data);
        String indexName = new String(data, StandardCharsets.UTF_8);

        nextLength = wrap.getInt();
        data = new byte[nextLength];
        wrap.get(data);
        String payloadData = new String(data, StandardCharsets.UTF_8);

        switch (operator) {
            case ADD_DOCUMENT -> {
                List<JSONObject> objects = JSON.parseArray(payloadData, com.alibaba.fastjson.JSONObject.class);
                storageManager.addDocuments(indexName,objects);
            }
            case DEL_DOCUMENT -> storageManager.deleteDocument(indexName,payloadData);
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
