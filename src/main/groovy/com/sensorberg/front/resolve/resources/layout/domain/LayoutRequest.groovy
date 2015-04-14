package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout request
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutRequest {
    String diRaw
    String deviceId
    DeviceIdentifier di
    String apiKey
    String geohash
    String geohashComputed
    String pid
    String remoteAddr
    String qos
    LayoutRequestBody activity
    boolean largeApplication
}
