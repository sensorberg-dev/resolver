package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.helpers.aspects.Monitored
import com.sensorberg.front.resolve.resources.layout.domain.Action
import com.sensorberg.front.resolve.resources.layout.domain.Beacon
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
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
import org.springframework.stereotype.Repository

import java.util.stream.Collectors

/**
 * elasticsearch layout producer
 */
@Slf4j
@Repository
class ESLayoutProvider implements LayoutHandler, IsSearchClient {

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
                .setIndices(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.beacon)
                .setQuery(
                QueryBuilders.matchQuery("applicationIds", applicationId)
        )
                .addAggregation(AggregationBuilders.terms("uuids").field("proximityUUID"))
                .addFields("major", "minor", "actionIds", "proximityUUID")
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
                            actionIds: it.fields.actionIds.values
                    )
                }
        ]
    }

    /**
     * use this layout resolve for large layouts or when you want to limit layout to given geohash
     * @param applicationId SDK applicationId
     * @param geohash geohash
     * @return layout
     */
    private def getBeaconsFast(String applicationId, String geohash) {
        def geoBox = getGeoHashBox(geohash)
        def results = client.prepareSearch()
                .setIndices(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.beacon)
                .setQuery(
                QueryBuilders.filteredQuery(
                        new MatchQueryBuilder("applicationIds", applicationId),
                        new GeoBoundingBoxFilterBuilder("location")
                                .topLeft(geoBox.topLeft)
                                .bottomRight(geoBox.bottomRight)
                                .cache(true)
                )
        )
                .addAggregation(AggregationBuilders.terms("uuids").field("proximityUUID"))
                .addFields("major", "minor", "actionIds", "proximityUUID")
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
                beacons       : results.hits.hits.collect { it.fields as Beacon }
        ]
    }

    /**
     * get beacon information from db for given request
     * @param request
     * @return
     */
    private Beacon getBeacon(LayoutRequest request) {
        def results = client.prepareSearch(ESConfig.INDEX_NAME).setTypes(ESConfig.INDEX.beacon).setQuery(
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

    private Collection<Action> getActions(Set<String> actionIds) {
        def result = client.prepareMultiGet().add(ESConfig.INDEX_NAME, ESConfig.INDEX.action, actionIds).get()
        result.responses.findAll {
            it.response.exists
        }.collect {
            it.response.asObject(Action)
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
    LayoutCtx getForBeacon(LayoutCtx ctx) {
        def request = ctx.request
        def beacon = getBeacon(request)
        // do we have geo position? if so use one from beacon, otherwise use provided with request
        request.geohashComputed = (beacon?.longitude != null && beacon?.longitude != null) ?
                GeoHashUtils.encode(beacon.latitude, beacon.longitude) :
                request.geohash
        return get(ctx)
    }

    @Override
    @Monitored
    LayoutCtx get(LayoutCtx ctx) {
        def request = ctx.request
        def results = (request.geohashComputed != null && request.largeApplication) ?
                getBeaconsFast(request.apiKey, request.geohashComputed) :
                getAllBeacons(request.apiKey)

        if (results?.beacons?.size() < 1) {
            ctx.response = new LayoutResponse()
            return ctx
        }

        def actionToBeacons = mapActionToBeacons(results.beacons)
        def actions = getActions(actionToBeacons.keySet())

        def finalResults = actions.stream().map { action ->
            def beaconUUIds = actionToBeacons.get(action.id)

            if (beaconUUIds == null) {
                return null
            }

            return [
                    eid           : action.id,
                    trigger       : action.triggers,
                    delay         : action.deliverAfter,
                    deliverAt     : action.deliverAt,
                    beacons       : beaconUUIds,
                    supressionTime: action.suppressionTime,
                    content       : action.payload,
                    type          : action.type,
                    timeframes    : action.timeframes,
                    sendOnlyOnce  : action.sendOnlyOnce,
                    typeString    : action.typeString()
            ].findAll { k, v -> v != null }
        }.filter { it != null }.collect(Collectors.toList())

        ctx.response = asLayoutResponse(results.proximityUUIDs, finalResults)
        return ctx
    }

    static LayoutResponse asLayoutResponse(Collection uuids, Collection results) {
        return new LayoutResponse(
                accountProximityUUIDs: uuids,
                actions: results
        )
    }
}