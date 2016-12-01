package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.helpers.aspects.Monitored
import com.sensorberg.front.resolve.resources.layout.domain.Action
import com.sensorberg.front.resolve.resources.layout.domain.Beacon
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.resources.layout.domain.LayoutAction
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequest
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.layout.domain.LayoutResponse
import groovy.util.logging.Slf4j
import org.elasticsearch.common.geo.GeoHashUtils
import org.elasticsearch.index.query.GeoBoundingBoxFilterBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import java.util.stream.Collectors

/**
 * elasticsearch layout producer
 */
@Slf4j
@Repository
class ESLayoutProvider implements LayoutHandler, IsSearchClient {

    @Autowired
    ESConfig esConfig

    // todo: move it to helper function
    private static def getGeoHashBox(String geohash) {
        def result = GeoHashUtils.neighbors(geohash)
        [
                topLeft    : result.first(),
                bottomRight: result.last()
        ]
    }

    private def getAllBeacons(String applicationId) {
        def results = client.prepareSearch()
                .setIndices(esConfig.getIndexName())
                .setTypes(esConfig.INDEX.beacon)
                .setSize(esConfig.MAX_SEARCH_RESULTS)
                .setQuery(
                QueryBuilders.matchQuery("applicationIds", applicationId)
        )
                .addAggregation(AggregationBuilders.terms("uuids").field("proximityUUID"))
                .addFields("major", "minor", "actionIds", "proximityUUID", "versionId")
                .execute().actionGet()

        if (results.hits.totalHits == 0) {
            return null
        }

        Terms uuids = results.aggregations.get("uuids")
        def proximityUUIDs = uuids.buckets.collect {
            it.key
        }

        return [
                proximityUUIDs: proximityUUIDs,
                beacons       : results.hits.hits.collect {
                    new Beacon(
                            proximityUUID: it.fields.proximityUUID.value,
                            major: it.fields.major.value,
                            minor: it.fields.minor.value,
                            actionIds: it.fields.actionIds.values,
                            versionId: it.fields.versionId.value
                    )
                }
        ]
    }

    /**
     * get beacon information from db for given request
     * @param request
     * @return
     */
    private Beacon getBeacon(LayoutRequest request) {
        def results = client.prepareSearch(esConfig.getIndexName()).setTypes(esConfig.INDEX.beacon).setQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("pid", request.pid))
                        .must(QueryBuilders.matchQuery("applicationIds", request.apiKey))
        ).setFrom(0).setSize(1)
                .execute().actionGet()

        if (results.hits.totalHits == 0) {
            return null
        }
        return results.hits.hits.first().asObject(Beacon)
    }

    private Collection<Action> getActions(Set<String> actionIds, String apiKey) {
        def result = client.prepareMultiGet().add(esConfig.getIndexName(), esConfig.INDEX.action, actionIds).get()
        result.responses.findAll {
            it.response.exists
        }.collect {
            it.response.asObject(Action)
        }.findAll { Action action ->
            action.applicationIds.contains(apiKey)
        }
    }

    private static Map<String, Collection<String>> mapActionToBeacons(Collection<Beacon> beacons) {
        Map<String, Collection<String>> result = new HashMap<>()

        beacons.each { beacon ->
            beacon.actionIds.each { action ->
                def value = result.computeIfAbsent(action, { new LinkedList<String>() })
                value.add(beacon.getPid())
            }
        }
        return result
    }

    @Override
    @Monitored
    LayoutCtx get(LayoutCtx ctx) {
        def request = ctx.request
        def results = getAllBeacons(request.apiKey)

        if (results?.beacons?.size() < 1) {
            ctx.response = new LayoutResponse()
            return ctx
        }

        def actionToBeacons = mapActionToBeacons(results.beacons)
        def actions = getActions(actionToBeacons.keySet(), request.apiKey)

        def finalResults = actions.collect { action ->
            def beaconUUIds = actionToBeacons.get(action.id)

            if (beaconUUIds == null) {
                return null
            }

            return new LayoutAction(
                    eid: action.id,
                    trigger: action.triggers,
                    delay: action.deliverAfter,
                    deliverAt: action.deliverAt,
                    beacons: beaconUUIds,
                    supressionTime: action.suppressionTime,
                    suppressionTime: action.suppressionTime,
                    content: action.payload,
                    type: action.type,
                    timeframes: action.timeframes,
                    sendOnlyOnce: action.sendOnlyOnce,
                    typeString: action.typeString()
            )
        }.findAll { action -> action != null }

        ctx.response = asLayoutResponse(results.proximityUUIDs, finalResults)
        return ctx
    }

    static LayoutResponse asLayoutResponse(Collection uuids, Collection<LayoutAction> results) {
        return new LayoutResponse(
                accountProximityUUIDs: uuids,
                actions: results
        )
    }
}