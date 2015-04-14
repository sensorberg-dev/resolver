package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.resources.layout.domain.Action
import com.sensorberg.front.resolve.resources.layout.domain.Beacon
import com.sensorberg.front.resolve.producers.els.ESClientProducer
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import groovy.json.JsonOutput
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.geo.GeoPoint
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
    ESClientProducer esClientProducer;

    boolean indexBeacons(Collection<Beacon> rawBeacons, long tillVersionId) {
        if(rawBeacons == null || rawBeacons.size() == 0) {
            return true
        }
        def beacons = [:]

        rawBeacons.forEach { result ->
            def beacon = beacons.getOrDefault(result.id, [
                    id            : result.id,
                    name          : result.name,
                    applicationId : result.applicationId,
                    companyId     : result.companyId,
                    pid           : String.format("%s%05d%05d", result.proximityUUID, result.major, result.minor),
                    versionId     : tillVersionId,
                    location      : (result.latitude != null && result.longitude != null) ? new GeoPoint(result.latitude, result.longitude) : null,
                    major         : result.major,
                    minor         : result.minor,
                    proximityUUID : result.proximityUUID,
                    applicationIds: [] as HashSet,
                    actionIds     : [] as HashSet
            ])
            beacon.applicationIds << result.apiKey
            beacon.actionIds << result.actionId
            beacons.put(beacon.id, beacon)
        }

        def bulk = client.prepareBulk().setRefresh(true)
        beacons.each { key, beacon ->
            def request = new IndexRequest(ESConfig.INDEX_NAME, ESConfig.INDEX.beacon)
                    .id(key)
                    .source(beacon)
            bulk.add(request)
        }
        return !bulk.execute().get().hasFailures()
    }

    boolean indexActions(Collection<Action> rawActions, long tillVersionId) {
        if(rawActions == null || rawActions.size() == 0) {
            return true
        }
        def bulk = client.prepareBulk().setRefresh(true)
        rawActions.each { action ->
            def triggers = 0
            if (action.triggerEntry) triggers |= 1 << 0
            if (action.triggerExit) triggers |= 1 << 1
            action.triggers = triggers
            action.versionId = tillVersionId
            def operation = (action.active) ?
                new IndexRequest(ESConfig.INDEX_NAME, ESConfig.INDEX.action).id(action.id).source(JsonOutput.toJson(action)) :
                new DeleteRequest(ESConfig.INDEX_NAME, ESConfig.INDEX.action, action.id)
            bulk.add(operation)
        }

        def response = bulk.get()
        if (response.hasFailures()) {
            println response.buildFailureMessage()
        }
        return !response.hasFailures()
    }

    boolean analyzeApplications() {
        def results = client
                .prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.beacon)
                .addAggregation(AggregationBuilders.terms("applicationSize").field("applicationIds").size(0))
                .execute().actionGet()
        def bulk = client.prepareBulk()
        Terms terms = results.aggregations.get("applicationSize")
        terms.buckets.each { bucket ->
            bulk.add(
                    new IndexRequest(ESConfig.INDEX_NAME, ESConfig.INDEX.application)
                            .id(bucket.key).source([
                            beacons: bucket.docCount
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

}
