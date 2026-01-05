package org.halosky.lucene;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.halosky.pojo.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StudyTest {



    @Test
    public void writeTest() throws IOException {
        // 分词器
        List<User> users = User.generatorUserList();

        List<Document> documents = new ArrayList<>();
        for (User user : users) {
            Document document = new Document();
            document.add(new TextField("name",user.getUserName(),Field.Store.YES));
            document.add(new IntField("age",user.getAge(),Field.Store.YES));
            document.add(new TextField("address",user.getWorkAddress(),Field.Store.YES));
            documents.add(document);
        }
        Analyzer analyzer = new StandardAnalyzer();

        Directory directory = FSDirectory.open(Path.of("/Users/yanghuan/core/project/java/searchelastics/indexs"));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        for (Document document : documents) {
            indexWriter.addDocument(document);
        }
        indexWriter.close();
    }


}
