package org.halosky.metadata;

import lombok.Data;

/**
 * packageName org.halosky.lucene
 *
 * @author huan.yang
 * @className IndexMetadata
 * @date 2026/1/7
 */
@Data
public class IndexMetadata {

    private String uuid;

    private long createTime;

    private int version;

    private IndexSettings indexSettings;

}
