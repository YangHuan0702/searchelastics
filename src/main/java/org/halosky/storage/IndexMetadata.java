package org.halosky.storage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.halosky.handler.IndexSetting;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * packageName org.halosky.storage
 *
 * @author huan.yang
 * @className IndexMetadata
 * @date 2026/1/12
 */
@Data
@Slf4j
public class IndexMetadata {

    private final String indexName;

    private Path indexPath;

    private IndexSetting indexSettings;

    private IndexWriter indexWriter;

    private final Directory directory;

    private final DirectoryReader directoryReader;

    private AtomicLong count = new AtomicLong(0);

    public static final Analyzer ANALYZER = new IKAnalyzer();

    private final IndexSearcher indexSearcher;

    public IndexMetadata(IndexSetting indexSettings, Path indexPath,String indexName) throws IOException {
        log.info("[IndexMetadata] initialization index-medaData settings:[{}], index-path:[{}] ", indexSettings, indexPath);
        if (indexPath.toFile().exists()) {
            Files.delete(indexPath);
        }

        directory = FSDirectory.open(indexPath);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ANALYZER);
        indexWriter = new IndexWriter(directory, indexWriterConfig);
        this.indexSettings = indexSettings;
        this.indexName = indexName;

        directoryReader = DirectoryReader.open(directory);
        indexSearcher = new IndexSearcher(directoryReader);
    }


    public void addDocument(List<Document> documentList) throws IOException {
        for (Document indexableFields : documentList) {
            indexWriter.addDocument(indexableFields);
        }
        count.addAndGet(documentList.size());
    }


    public void close() throws IOException {
        indexWriter.close();
        directoryReader.close();
        directory.close();
    }


}
