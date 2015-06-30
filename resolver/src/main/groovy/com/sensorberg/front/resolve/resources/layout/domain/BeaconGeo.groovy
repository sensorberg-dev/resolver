package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.elasticsearch.common.geo.GeoHashUtils
import org.elasticsearch.common.geo.GeoPoint

/**
 * beacon geo
 */
trait BeaconGeo {

    Double latitude
    Double longitude

    @JsonIgnore
    String getGeoHash() {
        return (hasLocation()) ? GeoHashUtils.encode(latitude, longitude) : null
    }

    @JsonIgnore
    GeoPoint getGeoPoint() {
        return (hasLocation()) ? new GeoPoint(latitude, longitude) : null
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180
    }

}