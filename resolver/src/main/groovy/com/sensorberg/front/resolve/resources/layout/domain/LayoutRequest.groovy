package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout request
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutRequest {
    String diRaw
    String deviceId
    String advertisingIdentifier
    DeviceIdentifier di
    String apiKey
    String geohash
    String geohashComputed
    String pid
    String remoteAddr
    String qos
    String etag
    LayoutRequestBody activity
    boolean largeApplication
    boolean invalid
}
