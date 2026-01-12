package org.halosky.query;

import org.apache.lucene.document.IntPoint;
import org.apache.lucene.search.Query;


/**
 * packageName org.halosky.query
 *
 * @author huan.yang
 * @className RangeQuery
 * @date 2026/1/12
 */
public final class RangeQuery implements QueryNode {

    private final String fieldName;

    private final int gte;

    private final int lte;


    public RangeQuery(String fieldName,int gte, int lte) {
        this.fieldName = fieldName;
        this.gte = gte;
        this.lte = lte;
    }


    @Override
    public Query toLuceneQuery() {
        return IntPoint.newRangeQuery(fieldName,gte,lte);
    }
}
