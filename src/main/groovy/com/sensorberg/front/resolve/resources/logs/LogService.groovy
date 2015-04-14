package com.sensorberg.front.resolve.resources.logs

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import groovy.json.JsonOutput
import org.elasticsearch.index.query.MatchAllQueryBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.stereotype.Service

/**
 * log service
 */
@Service
class LogService implements IsSearchClient {

    static enum TYPE {
        LAYOUT(ESConfig.INDEX.layoutLog),
        INDEX(ESConfig.INDEX.monitoringLog)

        private String indexName

        public TYPE(String indexName) {
            this.indexName = indexName
        }

        public String getIndexName() {
            return indexName
        }
    }

    public void log(LayoutCtx ctx) {
        client.prepareIndex(ESConfig.INDEX_NAME, TYPE.LAYOUT.indexName)
                .setSource(JsonOutput.toJson(ctx))
                .execute().actionGet()
    }

    public def getLayoutLogs() {
        def results = client
                .prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.layoutLog)
                .setQuery(new MatchAllQueryBuilder())
                .addSort(new FieldSortBuilder("eventDate").order(SortOrder.DESC))
                .setSize(100)
                .get()
        results.hits.hits.collect { hit ->
            def response = hit.source
            response.id = hit.id
            return response
        }
    }

    public def getLayoutLog(String id) {
        def result = client.prepareGet(ESConfig.INDEX_NAME, ESConfig.INDEX.layoutLog, id)
        def response = result.source
        response.id = result.id
        return response
    }

}
