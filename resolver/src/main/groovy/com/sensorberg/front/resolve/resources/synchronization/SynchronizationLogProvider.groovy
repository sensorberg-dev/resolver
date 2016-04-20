package com.sensorberg.front.resolve.resources.synchronization

import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.helpers.aspects.Monitored
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationLogItem
import org.apache.commons.lang3.StringUtils
import org.elasticsearch.index.query.MatchAllQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * synchronization log provider is responsible for synchronization logs
 */
@Service
class SynchronizationLogProvider implements IsSearchClient {

    @Autowired
    ObjectMapper mapper

    @Autowired
    ESConfig esConfig

    @Monitored
    public SynchronizationLogItem getLastLogItem(SyncApplicationRequest sa) {
        def queryResult = client
                .prepareSearch(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.synchronizationLog)
                .setQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("synchronizationId", sa.id))
                .must(QueryBuilders.matchQuery("status", true))
        )
        //.setQuery(QueryBuilders.matchQuery("synchronizationId", sa.id))
                .addSort("tillVersionId", SortOrder.DESC)
                .setSize(1).get()
        return (queryResult.hits.size() == 1) ?
                queryResult.hits.first().asObject(SynchronizationLogItem) :
                new SynchronizationLogItem(tillVersionId: 0)
    }

    @Monitored
    public SynchronizationLogItem putLogItem(SynchronizationLogItem item) {
        def response = client
                .prepareIndex(esConfig.getIndexName(), esConfig.INDEX.synchronizationLog)
                .setRefresh(true)
                .setSource(mapper.writeValueAsBytes(item))
                .setTTL(ESConfig.TTL_SYNCHRONIZATION_LOG)
                .execute().actionGet()
        return (response.created) ? item : null
    }

    public Collection<SynchronizationLogItem> recentLogs(int size = 10, int from = 0) {
        def results = client.prepareSearch(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.synchronizationLog)
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort("synchronizationDate", SortOrder.DESC)
                .setSize(size)
                .setFrom(from).get()
        if (results.hits.totalHits == 0) {
            return []
        }
        results.hits.hits.collect { result -> result.asObject(SynchronizationLogItem) }
    }

    public SyncApplicationRequest saveSyncApplication(SyncApplicationRequest syncApp) {
        if(StringUtils.isEmpty(syncApp.id)) {
            def response = client.prepareIndex(esConfig.getIndexName(), esConfig.INDEX.syncApplications)
                    .setSource(mapper.writeValueAsBytes(syncApp))
                    .get()
            syncApp.id = response.id
            return syncApp
        }
        client.prepareUpdate(esConfig.getIndexName(), esConfig.INDEX.syncApplications, syncApp.id)
                .setDoc(mapper.writeValueAsBytes(syncApp))
                .setDocAsUpsert(true)
                .get()
        return syncApp
    }

    public List<SyncApplicationRequest> listSynchronizations() {
        def result = client.prepareSearch(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.syncApplications)
                .setSize(esConfig.MAX_SEARCH_RESULTS)
                .setQuery(new MatchAllQueryBuilder())
                .get()
        if (result.hits.totalHits == 0) {
            return []
        }

        return result.hits.hits.collect { item ->
            def response = item.asObject(SyncApplicationRequest)
            response.id = item.id
            return response
        }
    }

    public boolean delete(String synchronizationId) {
        client.prepareDeleteByQuery(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.syncApplications)
                .setQuery(QueryBuilders.matchQuery("_id", synchronizationId))
                .execute().actionGet();
        return true;
    }

    public SyncApplicationRequest getById(String id) {
        client.prepareGet(esConfig.getIndexName(), esConfig.INDEX.syncApplications, id).get().asObject(SyncApplicationRequest)
    }
}
