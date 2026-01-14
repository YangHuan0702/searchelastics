package org.halosky.server;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.halosky.server.protocol.SyncMessage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * packageName org.halosky.server
 *
 * @author huan.yang
 * @className ServerRequestCoordinate
 * @date 2026/1/14
 */
@Slf4j
public class PingPongCoordinate {

    private PingPongCoordinate(){}

    private static final PingPongCoordinate pingPongCoordinate = new PingPongCoordinate();

    public static PingPongCoordinate getInstance() {
        return pingPongCoordinate;
    }

    private final AtomicLong requestIdGenerator = new AtomicLong(1);

    private final Map<Long, Promise<SyncMessage>> requestMapping = new ConcurrentHashMap<>();

    public Long getRequestId() {
        return requestIdGenerator.getAndIncrement();
    }



    public SyncMessage addWatcherRequest(Channel channel,SyncMessage syncMessage, final Long requestId) throws Exception {

        if(Objects.isNull(requestId)) throw new NullPointerException("request-id is not null.");

        Promise<SyncMessage> currentRequestPromise = new DefaultPromise<>(channel.eventLoop());

        requestMapping.put(requestId,currentRequestPromise);

        channel.writeAndFlush(syncMessage).addListener(f -> {
            if(!f.isSuccess()) {
                requestMapping.remove(requestId);
                currentRequestPromise.setFailure(f.cause());
            }
        });

        if (!currentRequestPromise.await(60000)) {
            requestMapping.remove(requestId);
            throw new TimeoutException("request timeout");
        }

        if (!currentRequestPromise.isSuccess()) {
            throw new RuntimeException(currentRequestPromise.cause());
        }

        return currentRequestPromise.getNow();
    }

    public void notifyWaitRequest(long requestId,SyncMessage syncMessage) {
        Promise<SyncMessage> promise = requestMapping.remove(requestId);
        if (promise != null) {
            promise.setSuccess(syncMessage);
        }
    }

}
