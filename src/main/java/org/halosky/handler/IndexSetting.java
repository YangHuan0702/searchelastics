package org.halosky.handler;

import lombok.Data;

/**
 * packageName org.halosky.handler
 *
 * @author huan.yang
 * @className IndexSetting
 * @date 2026/1/12
 */
@Data
public class IndexSetting {

    private Integer num_of_shards = 1;

    private String codec = "LZ4";

    private Integer routing_partition_size;

}
