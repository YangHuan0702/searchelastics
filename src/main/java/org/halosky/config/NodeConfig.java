package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className NodeConfig
 * @date 2026/1/6
 */
@Data
public class NodeConfig {


    private String name;

    private boolean master;

    private boolean data;

}
