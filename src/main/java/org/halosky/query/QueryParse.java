package org.halosky.query;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * packageName org.halosky.query
 *
 * @author huan.yang
 * @className QueryParse
 * @date 2026/1/12
 */
@Slf4j
public class QueryParse {

    public static QueryNode parse(JsonNode node) {
        if (Objects.isNull(node) || node.isNull()) return null;
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        if (!fields.hasNext()) {
            throw new IllegalArgumentException("empty query object");
        }

        Map.Entry<String, JsonNode> entry = fields.next();

        if (fields.hasNext()) {
            throw new IllegalArgumentException("Query object must have exactly one field");
        }

        String queryType = entry.getKey();
        JsonNode body = entry.getValue();

        return switch (queryType) {
            case "term" -> parseTerm(body);
            case "range" -> parseRange(body);
            case "bool" -> parseBool(body);
            case "text" -> parseText(body);
            default -> throw new IllegalArgumentException("unknown query type");
        };
    }

    private static QueryNode parseText(JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("text query must be an object.");
        }
        Set<Map.Entry<String, JsonNode>> properties = body.properties();
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("text query is empty.");
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = properties.iterator();
        Map.Entry<String, JsonNode> next = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalArgumentException("text query must have exactly one field");
        }
        String key = next.getKey();
        String value = next.getValue().asText();
        return new TextQuery(key, value);
    }

    private static QueryNode parseBool(JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("bool query must be an object.");
        }


        List<QueryNode> must = parseArray(body, "must");
        List<QueryNode> should = parseArray(body, "should");
        List<QueryNode> mustNot = parseArray(body, "must_not");
        return new BooleanQuery(must, should, mustNot);
    }

    private static List<QueryNode> parseArray(JsonNode body, String name) {
        if (!body.has(name)) throw new IllegalArgumentException("prase array error, current name is [" + name + "]");

        JsonNode arrayNode = body.get(name);
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException("prase array error, current name is [" + name + "]");
        }
        List<QueryNode> array = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            array.add(parse(node));
        }
        return array;
    }

    private static QueryNode parseRange(JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("range query must be an object.");
        }
        Set<Map.Entry<String, JsonNode>> properties = body.properties();
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("range query is empty.");
        }
        Iterator<Map.Entry<String, JsonNode>> iterator = properties.iterator();
        Map.Entry<String, JsonNode> next = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalArgumentException("range query must have exactly one field");
        }
        String fieldName = next.getKey();
        return parseRangeValue(fieldName, next.getValue());
    }


    private static QueryNode parseRangeValue(String fieldName, JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("parse range value must be an object.");
        }
        Set<Map.Entry<String, JsonNode>> properties = body.properties();
        if (properties.size() != 2) {
            throw new IllegalArgumentException("parse range query is empty or illegal.");
        }

        int lte = -1;
        int gte = -1;
        for (Map.Entry<String, JsonNode> property : properties) {
            if (property.getKey().equals("lte")) {
                lte = property.getValue().asInt();
            } else if (property.getKey().equals("gte")) {
                gte = property.getValue().asInt();
            } else {
                throw new IllegalArgumentException("parse range query is illegal. unknow + [" + property.getKey() + "]");
            }
        }
        return new RangeQuery(fieldName, gte, lte);
    }


    private static QueryNode parseTerm(JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("term query must be an object.");
        }
        Set<Map.Entry<String, JsonNode>> properties = body.properties();
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("term query is empty.");
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = properties.iterator();
        Map.Entry<String, JsonNode> next = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalArgumentException("term query must have exactly one field");
        }
        String key = next.getKey();
        String value = next.getValue().asText();
        return new TermQuery(key, value);
    }

}
