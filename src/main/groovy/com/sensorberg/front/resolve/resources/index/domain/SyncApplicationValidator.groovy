package com.sensorberg.front.resolve.resources.index.domain

import org.apache.commons.lang3.StringUtils

/**
 * application synchronization request validator
 * probably we should just use bean validation here
 */
class SyncApplicationValidator {
    static public boolean isValid(SyncApplicationRequest app) {
        StringUtils.isNotEmpty(app.host) && StringUtils.isNotEmpty(app.apiKey)
    }
}
