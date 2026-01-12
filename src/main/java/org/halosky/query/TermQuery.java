package org.halosky.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * packageName org.halosky.query
 *
 * @author huan.yang
 * @className TermQuery
 * @date 2026/1/12
 */
public final class TermQuery implements QueryNode {

    private final String fieldName;
    private final String fieldValue;

    public TermQuery(String fieldName,String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }


    @Override
    public Query toLuceneQuery() {
        return new org.apache.lucene.search.TermQuery(new Term(fieldName,fieldValue));
    }
}
