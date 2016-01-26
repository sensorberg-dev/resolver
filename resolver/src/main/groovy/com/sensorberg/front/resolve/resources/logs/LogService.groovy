package com.sensorberg.front.resolve.resources.logs
import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.service.AzureEventHubService
import org.elasticsearch.index.query.MatchAllQueryBuilder
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
/**
 * log service
 */
@Service
class LogService implements IsSearchClient {

    @Autowired
    ObjectMapper mapper

    @Autowired
    AzureEventHubService azureEventHubService

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

        // Write to elastic search
        client.prepareIndex(ESConfig.INDEX_NAME, TYPE.LAYOUT.indexName, ctx.id)
                .setSource(mapper.writeValueAsBytes(ctx))
                .setTTL(ESConfig.TTL_LOG)
                .execute().actionGet()

        // Write to azure event hub
        azureEventHubService.sendObjectMessage(ctx);
    }

    public def getLayoutLogs(int from = 0, int size = 100, int slow = 0) {
        def query = (slow == 0) ? new MatchAllQueryBuilder() : new RangeQueryBuilder("elapsedTime").from(slow)
        def results = client
                .prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.layoutLog)
                .setQuery(query)
                .addSort(new FieldSortBuilder("eventDate").order(SortOrder.DESC))
                .setFrom(from)
                .setSize(size)
                .get()
        return results.hits.hits.collect { hit ->
            def response = hit.source
            response.id = hit.id
            return response
        }
    }

    public def getLayoutLog(String id) {
        def result = client.prepareGet(ESConfig.INDEX_NAME, ESConfig.INDEX.layoutLog, id).get()
        def response = result.source
        response.id = result.id
        return response
    }

    def deleteAll() {
        client
                .prepareDeleteByQuery(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.layoutLog)
                .setQuery(new MatchAllQueryBuilder())
                .get()
    }
}
