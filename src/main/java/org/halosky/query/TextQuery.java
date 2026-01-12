package org.halosky.query;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.halosky.storage.IndexMetadata;

/**
 * packageName org.halosky.query
 *
 * @author huan.yang
 * @className TextQuery
 * @date 2026/1/12
 */
@Slf4j
public final class TextQuery implements QueryNode{

    private final String fieldName;
    private final String fieldValue;

    public TextQuery(String fieldName,String fieldValue){
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }


    @Override
    public Query toLuceneQuery() {
        try{
            return new QueryParser(fieldName, IndexMetadata.ANALYZER).parse(fieldValue);
        } catch (Exception ex) {
            log.error("[TextQuery] to lucene query parse error:{}",ex.getMessage(),ex);
        }
        return null;
    }
}
