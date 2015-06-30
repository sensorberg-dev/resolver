package com.sensorberg.front.resolve.resources.index

import org.springframework.stereotype.Service

/**
 * version service for fast ETag support
 */
@Service
class VersionService {
    // todo: replace with cache
    Map<String, Long> versions = new HashMap<>()

    void put(String apiKey, long tillVersionId) {
        versions.put(apiKey, tillVersionId)
    }

    void putAll(Map<String, Long> apiKeys) {
        versions.putAll(apiKeys)
    }

    boolean hasCurrentVersion(String application, String etagString) {
        def currentVersion = versions.getOrDefault(application, 0)
        def etag = toEtag(etagString)
        return currentVersion > 0 && currentVersion <= etag
    }

    long get(String application) {
        return versions.get(application)
    }

    static long toEtag(String etag) {
        try {
            return etag?.toLong() ?: -1
        } catch (NumberFormatException ignored) {
            return -1
        }
    }

}
