package org.halosky.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * packageName org.halosky.lucene
 *
 * @author huan.yang
 * @className QueryTest
 * @date 2026/1/14
 */
public class QueryTest {


    @Test
    public void matchTest() throws IOException, ParseException {
        Analyzer analyzer = new CJKAnalyzer();

        Path path = Path.of("D:\\searchelastics\\node2\\data\\books");
        Directory directory = FSDirectory.open(path);
        try (DirectoryReader directoryReader = DirectoryReader.open(directory)) {
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
            QueryParser queryParse = new QueryParser("name", analyzer);
            Query parse = queryParse.parse("全国富婆通讯录");

            TermQuery termQuery = new TermQuery(new Term("name", "全国富婆通讯录"));
            ScoreDoc[] scoreDocs = indexSearcher.search(parse, 10).scoreDocs;

            StoredFields storedFields = indexSearcher.storedFields();
            if (null != scoreDocs) {
                for (ScoreDoc scoreDoc : scoreDocs) {
                    Document doc = storedFields.document(scoreDoc.doc);
                    System.out.println(doc);
                }
            }

            System.out.println("index path = " + path.toAbsolutePath());
            System.out.println("maxDoc = " + directoryReader.maxDoc());
            System.out.println("numDocs = " + directoryReader.numDocs());

        } finally {
            directory.close();
        }
    }

}
