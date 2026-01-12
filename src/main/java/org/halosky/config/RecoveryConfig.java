package org.halosky.config;

import lombok.Data;

/**
 * packageName org.halosky.config
 *
 * @author huan.yang
 * @className RecoveryConfig
 * @date 2026/1/6
 */
@Data
public class RecoveryConfig {

    private int nodeInitialPrimariesRecoveries;

    private int maxSizePerSec;

    private int concurrentStreams;

}
