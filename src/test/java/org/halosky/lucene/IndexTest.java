package org.halosky.lucene;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * packageName org.halosky.lucene
 *
 * @author huan.yang
 * @className IndexTest
 * @date 2026/1/7
 */
public class IndexTest {


    @Test
    public void indexTest() {

        Path indexPath = Paths.get("D:/searchelatics");
        try (Directory directory = FSDirectory.open(indexPath)) {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig();
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
