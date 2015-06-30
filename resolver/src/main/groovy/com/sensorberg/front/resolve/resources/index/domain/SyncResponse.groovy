package com.sensorberg.front.resolve.resources.index.domain

class SyncResponse<T> {
    long requestedVersionId
    long tillVersionId
    Collection<T> items
}
