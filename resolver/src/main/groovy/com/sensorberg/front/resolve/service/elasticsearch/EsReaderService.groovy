package com.sensorberg.front.resolve.service.elasticsearch

import com.sensorberg.groovy.helper.Looper
import groovy.util.logging.Slf4j;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;


import com.sensorberg.front.resolve.config.ESConfig;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Andreas Dörner on 01.03.16.
 */

@Slf4j
@Service
public class EsReaderService {

    @Autowired
    Client client;

    @Autowired
    ESConfig esConfig;

    /**
     * Read all from ES with type entry
     *
     */
    public List<Map<String, Object>> readEntries(final Date from, final Date to) {

        org.joda.time.DateTime fromJoda = new org.joda.time.DateTime(from);
        org.joda.time.DateTime toJoda = new org.joda.time.DateTime(to);

        log.info(fromJoda.toString() + " - " + toJoda.toString());

        List<Map<String, Object>> result = new ArrayList<>();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(rangeQuery("layoutLog.eventDate").gte(fromJoda.toString()).lte(toJoda.toString()));

        try {
            SearchRequestBuilder sizeQuery = client.prepareSearch(esConfig.getIndexName())
                    .setTypes(ESConfig.INDEX.layoutLog)
                    .setSearchType(SearchType.SCAN)
                    .setSize(10)
                    .setScroll(TimeValue.timeValueSeconds(60))
                    .setQuery(queryBuilder);

            SearchResponse scrollResponse = sizeQuery.execute().actionGet();
            long totalHits = scrollResponse.getHits().getTotalHits();

            String scrollID = scrollResponse.getScrollId();
            SearchResponse response;

            Looper.loop {
                response = client.prepareSearchScroll(scrollID)
                        .setScroll(TimeValue.timeValueSeconds(60))
                        .get();
                scrollID = response.getScrollId();
                for (SearchHit searchHit : response.getHits()) {
                    // Source as map
                    Map<String, Object> resultMap = searchHit.getSource();
                    result.add(resultMap);
                }
            }.until {
                response.getHits().hits().length <= 0
            }


            if (result.size() == totalHits){
                log.info("size matches perfectly");
            } else {
                log.error("size is different!!!");
            }
        } catch (Exception e) {
            log.warn("cannot get list of entries from ES: [reason: {}]", e.getMessage());
            throw new SearchException(e.getMessage());
        } finally {
            return result;
        }
    }
}
