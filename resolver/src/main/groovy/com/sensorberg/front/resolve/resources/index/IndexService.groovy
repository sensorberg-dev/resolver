package com.sensorberg.front.resolve.resources.index
import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.producers.els.ESClientProducer
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.layout.domain.Action
import com.sensorberg.front.resolve.resources.layout.domain.Beacon
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
/**
 * index service responsible for layout synchronization with backend
 * in order to enable synchronization in the first place you need to go to
 * synchronization resource and add one
 */
@Service
class IndexService implements IsSearchClient {

    @Autowired
    ESClientProducer esClientProducer

    @Autowired
    VersionService versionService

    @Autowired
    ObjectMapper mapper

    @Autowired
    ESConfig esConfig

    boolean indexBeacons(Collection<Beacon> rawBeacons, SyncApplicationRequest sa, long tillVersionId) {
        if(rawBeacons == null || rawBeacons.size() == 0) {
            return true
        }
        Map<String, Beacon> beacons = [:]
        Map<String, Long> apiKeyVersions = [:]

        rawBeacons.forEach { result ->
            def beacon = beacons.getOrDefault(result.id, new Beacon(
                    id            : result.id,
                    name          : result.name,
                    applicationId : result.applicationId,
                    companyId     : result.companyId,
                    pid           : String.format("%32s%05d%05d", result.proximityUUID, result.major, result.minor),
                    versionId     : tillVersionId,
                    location      : result.getGeoHash(),
                    major         : result.major,
                    minor         : result.minor,
                    proximityUUID : result.proximityUUID,
                    applicationIds: [] as HashSet,
                    actionIds     : [] as HashSet,
                    environment   : sa.id,
                    active        : result.active
            ))
            beacon.applicationIds << result.apiKey
            beacon.actionIds << result.actionId
            beacons.put(beacon.id, beacon)
            apiKeyVersions.put(result.apiKey, tillVersionId)
        }

        try {
            def bulk = client.prepareBulk().setRefresh(true)
            beacons.each { key, beacon ->
                def request = (beacon.active) ?
                        new IndexRequest(esConfig.getIndexName(), esConfig.INDEX.beacon, beacon.id).source(mapper.writeValueAsBytes(beacon)) :
                        new DeleteRequest(esConfig.getIndexName(), esConfig.INDEX.beacon, beacon.id)
                bulk.add(request)
            }
            versionService.putAll(apiKeyVersions)
            return !bulk.execute().get().hasFailures()
        } catch (Exception ignored) {
            return false
        }
    }

    boolean indexActions(Collection<Action> rawActions, long tillVersionId) {
        if(rawActions == null || rawActions.size() == 0) {
            return true
        }

        Map<String, Action> actions = [:]
        rawActions.each { item ->
            def action = actions.getOrDefault(item.id, item)
            action.applicationIds << item.apiKey
            actions.put(action.id, action)
        }

        try {
            def bulk = client.prepareBulk().setRefresh(true)
            actions.values().each { action ->
                def triggers = 0
                if (action.triggerEntry) triggers |= 1 << 0
                if (action.triggerExit) triggers |= 1 << 1
                action.triggers = triggers
                action.versionId = tillVersionId
                def operation = (action.active) ?
                        new IndexRequest(esConfig.getIndexName(), esConfig.INDEX.action).id(action.id).source(mapper.writeValueAsBytes(action)) :
                        new DeleteRequest(esConfig.getIndexName(), esConfig.INDEX.action, action.id)
                bulk.add(operation)
            }
            return !bulk.get().hasFailures()
        } catch (Exception ignored) {
            return false
        }
    }

    boolean analyzeApplications() {
        def results = client
                .prepareSearch(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.beacon)
                .addAggregation(
                    AggregationBuilders.terms("applicationSize").field("applicationIds").size(0)
                        .subAggregation(AggregationBuilders.terms("environments").field("environment").size(0))
                )
                .execute().actionGet()
        def bulk = client.prepareBulk()
        Terms terms = results.aggregations.get("applicationSize")
        terms.buckets.each { bucket ->
            bulk.add(
                    new IndexRequest(esConfig.getIndexName(), esConfig.INDEX.application)
                            .id(bucket.key).source([
                            beacons: bucket.docCount,
                            environment: bucket.aggregations.get("environments")?.buckets[0].key
                    ])
            )
        }
        def response = bulk.get()
        if (response.hasFailures()) {
            println response.buildFailureMessage()
        }
        return !response.hasFailures()
    }

    def reset() {
        esClientProducer.forceRecreateIndexes()
    }

    def analyzeApplication(SyncApplicationRequest sa) {
        def results = client
                .prepareSearch(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.beacon)
                .setQuery(QueryBuilders.matchQuery("applicationId", sa.apiKey))
                .addAggregation(AggregationBuilders.terms("applicationSize").field("applicationIds").size(0))
                .execute().actionGet()
        Terms terms = results.aggregations.get("applicationSize")

        client
                .prepareIndex(esConfig.getIndexName(), esConfig.INDEX.application)
                .setId(sa.apiKey)
                .setSource([
                        beacons: 1
                ])

    }

}
