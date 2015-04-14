package com.sensorberg.front.resolve.resources.index.domain

/**
 * synchronization history request
 */
class SyncHistoryRequest {
    String id
    String diRaw
    String apiKey
    String deviceId
    String geohash
    String remoteAddr
    List<SyncHistoryEvent> events

}
