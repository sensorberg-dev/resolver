package com.sensorberg.front.resolve.resources.index

import org.springframework.stereotype.Service

/**
 * version service for fast ETag support
 */
@Service
class VersionService {

    Map<String, Long> versions = new HashMap<>()

    void put(String application, long version) {
        versions.put(application, version)
    }

    long get(String application) {
        return versions.get(application)
    }

}
