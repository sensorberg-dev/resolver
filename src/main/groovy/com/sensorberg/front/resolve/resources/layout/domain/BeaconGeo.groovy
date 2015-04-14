package com.sensorberg.front.resolve.resources.layout.domain

import org.elasticsearch.common.geo.GeoHashUtils

/**
 * beacon geo
 */
trait BeaconGeo {

    Double latitude
    Double longitude

    String getGeoHash() {
        return GeoHashUtils.encode(latitude, longitude)
    }

}