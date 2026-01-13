package org.halosky.shard;

import lombok.extern.slf4j.Slf4j;
import org.halosky.config.IndexConfig;

import java.util.Objects;

/**
 * packageName org.halosky.shard
 *
 * @author huan.yang
 * @className RoutingManager
 * @date 2026/1/13
 */
@Slf4j
public class RoutingManager {

    private final IndexConfig indexConfig;

    public RoutingManager(IndexConfig indexConfig) {
        this.indexConfig = indexConfig;
    }


    public int routing(String documentId) {
        if(Objects.isNull(documentId)) {
            throw new NullPointerException("documentId is null");
        }
        return documentId.hashCode() % indexConfig.getNumberOfShards();
    }

}
