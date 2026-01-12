package org.halosky.query;

import org.apache.lucene.search.Query;

/**
 * packageName org.halosky.query
 *
 * @author huan.yang
 * @className QueryNode
 * @date 2026/1/12
 */

public sealed interface QueryNode permits BooleanQuery,RangeQuery,TermQuery,TextQuery {

    Query toLuceneQuery();

}
