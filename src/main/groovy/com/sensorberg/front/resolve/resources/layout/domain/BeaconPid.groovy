package com.sensorberg.front.resolve.resources.layout.domain

/**
 * iBeacon ProximityUUID Major Minor
 * pid is just a lower cased string without any separators
 */
trait BeaconPid {
    Integer major
    Integer minor
    String proximityUUID
    String pid

    String getPid() {
        return String.format("%32s%05d%05d", proximityUUID, major, minor)
    }
}
