package com.sensorberg.front.resolve.resources.index.domain

import com.sensorberg.front.resolve.resources.layout.domain.Action
import com.sensorberg.front.resolve.resources.layout.domain.Beacon

class SynchronizationResponse {
    long requestedVersionId
    long tillVersionId
    Collection<Beacon> beacons
    Collection<Action> actions

}
