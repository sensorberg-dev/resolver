package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * actual beacon :)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Beacon implements BeaconPid, BeaconGeo, HasVersion {
    /**
     * beacon id
     */
    String id
    /**
     * name
     */
    String name
    /**
     * company deep link id like for example your company database id for reference
     */
    String companyId
    /**
     * actionId string
     */
    String actionId
    /**
     * actions assigned to beacon
     */
    Collection<String> actionIds
    /**
     * application deep link id
     */
    String applicationId
    /**
     * application api key
     */
    String apiKey

}
