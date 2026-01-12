package org.halosky.metadata;

import lombok.Data;

/**
 * packageName org.halosky.lucene
 *
 * @author huan.yang
 * @className IndexSettings
 * @date 2026/1/7
 */
@Data
public class IndexSettings {

    private Integer numberOfShards;

    private String codec;

    private Integer routingPartitionSize;

    private Integer numberOfReplicas;
}
