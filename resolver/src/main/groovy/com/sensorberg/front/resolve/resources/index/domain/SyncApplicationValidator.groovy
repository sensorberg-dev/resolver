package com.sensorberg.front.resolve.resources.index.domain

import org.apache.commons.lang3.StringUtils

/**
 * syncApplicationRequest synchronization request validator
 * probably we should just use bean validation here
 */
class SyncApplicationValidator {
    static public boolean isValid(SyncApplicationRequest app) {
        if(StringUtils.isEmpty(app.url)) {
            return false
        }
        try {
            new URI(app.url)
            return true
        } catch (ignored) {
            return false
        }
    }
}
