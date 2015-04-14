package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.helpers.aspects.Monitored
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationLogItem
import groovy.json.JsonOutput
import org.elasticsearch.common.joda.time.DateTime
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
    VersionService versionService

    @Monitored
    public SynchronizationLogItem getLastLogItem(SyncApplicationRequest sa) {
        def queryResult = client
                .prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.synchronizationLog)
                .setQuery(QueryBuilders.matchQuery("environment", sa.environment))
                .addSort("tillVersionId", SortOrder.DESC)
                .setSize(1).get()
        return (queryResult.hits.size() == 1) ? queryResult.hits.first().source as SynchronizationLogItem : null
    }

    @Monitored
    public SynchronizationLogItem putLogItem(SynchronizationLogItem item) {
        if(item.changedItems > 0) {
            /*
             * todo: review this entry - it is responsible for keeping synchronization version
              * this will be used for fast ETag support
              */
            versionService.put(item.environment, item.tillVersionId)
        }
        def response = client
                .prepareIndex(ESConfig.INDEX_NAME, ESConfig.INDEX.synchronizationLog)
                .setRefresh(true)
                .setSource(JsonOutput.toJson(item)).execute().actionGet()
        return (response.created) ? item : null
    }

    public Collection<SynchronizationLogItem> recentLogs() {
        def results = client.prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.synchronizationLog)
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort("synchronizationDate", SortOrder.DESC)
                .setSize(10).get()
        if (results.hits.totalHits == 0) {
            return []
        }
        results.hits.hits.collect { result ->
            new SynchronizationLogItem(
                    environment: result.source.environment,
                    tillVersionId: result.source.tillVersionId,
                    synchronizationDate: DateTime.parse(result.source.synchronizationDate)?.toDate(),
                    status: result.source.status,
                    statusDetails: result.source.statusDetails
            )
        }
    }

    public SyncApplicationRequest addSyncApplication(SyncApplicationRequest syncApp) {
        def syncApplication = [
                host  : syncApp.host,
                apiKey: syncApp.apiKey,
                token : syncApp.token
        ] as SyncApplicationRequest
        def result = client.prepareIndex(ESConfig.INDEX_NAME, ESConfig.INDEX.syncApplications)
                .setId(syncApp.environment)
                .setSource(JsonOutput.toJson(syncApplication)).execute().actionGet()
        (result.created) ? syncApplication : null
    }

    public List<SyncApplicationRequest> listSyncApplications() {
        def result = client.prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.syncApplications)
                .setQuery(new MatchAllQueryBuilder())
                .get()
        if (result.hits.totalHits == 0) {
            return []
        }

        return result.hits.hits.collect { item ->
            [
                    host: item.source.host,
                    apiKey: item.source.apiKey,
                    token: item.source.token
            ] as SyncApplicationRequest
        }
    }

    public boolean delete(String apiKey) {
        client.prepareDeleteByQuery(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.syncApplications)
                .setQuery(QueryBuilders.matchQuery("apiKey", apiKey))
                .execute().actionGet();
        return true;
    }

}
