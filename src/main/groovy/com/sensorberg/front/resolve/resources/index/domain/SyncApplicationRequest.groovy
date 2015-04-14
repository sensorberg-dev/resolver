package com.sensorberg.front.resolve.resources.index.domain

/**
 * application synchronization request
 */
class SyncApplicationRequest {
    /**
     * synchronization host like https://connect.sensorberg.com/
     */
    String host
    /**
     * your application api key - to get one go for example to manage.sensorberg.com > apps
     */
    String apiKey
    /**
     * synchronization token - keep it empty
     */
    String token

    String getEnvironment() {
        return "${host}/${apiKey}"
    }


}
