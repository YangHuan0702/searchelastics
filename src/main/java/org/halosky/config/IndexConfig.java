package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className IndexConfig
 * @date 2026/1/6
 */
@Data
public class IndexConfig {

    private int numberOfShards;

    private int numberOfReplicas;
}
