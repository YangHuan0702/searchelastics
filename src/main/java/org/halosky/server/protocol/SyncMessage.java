package org.halosky.server.protocol;

import lombok.Data;


/**
 * packageName org.halosky.server.protocol
 *
 * @author huan.yang
 * @className SyncMessage
 * @date 2026/1/13
 */
@Data
public class SyncMessage {

    private int cmd;

    private byte[] payload;
}
