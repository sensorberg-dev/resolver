package com.sensorberg.front.resolve.resources.index.domain

/**
 * syncApplicationRequest synchronization request
 */
class SyncApplicationRequest {
    /**
     * synchronization id
     */
    String id
    /**
     * synchronization host like https://connect.sensorberg.com/
     */
    String url
    /**
     * this one will be used to send back data about user activity
     * when not set system will NOT send back any data about user activity
     */
    String backchannelUrl
}
