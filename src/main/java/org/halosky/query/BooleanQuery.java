package org.halosky.query;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

import java.util.List;
import java.util.Objects;

/**
 * packageName org.halosky.query
 *
 * @author huan.yang
 * @className BooleanQuery
 * @date 2026/1/12
 */
public final class BooleanQuery implements QueryNode {

    private final List<QueryNode> must;

    private final List<QueryNode> should;

    private final List<QueryNode> must_not;


    public BooleanQuery(List<QueryNode> must,List<QueryNode> should,List<QueryNode> must_node) {
        this.must = must;
        this.should = should;
        this.must_not = must_node;
    }


    @Override
    public Query toLuceneQuery() {
        org.apache.lucene.search.BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();

        if(Objects.nonNull(must) && !must.isEmpty()) {
            must.forEach(q -> builder.add(q.toLuceneQuery(),BooleanClause.Occur.MUST));
        }
        if(Objects.nonNull(should) && !should.isEmpty()) {
            should.forEach(q -> builder.add(q.toLuceneQuery(),BooleanClause.Occur.SHOULD));
        }
        if(Objects.nonNull(must_not) && !must_not.isEmpty()) {
            must_not.forEach(q -> builder.add(q.toLuceneQuery(),BooleanClause.Occur.MUST_NOT));
        }
        return builder.build();
    }
}
