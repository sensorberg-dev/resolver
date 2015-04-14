package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout response
 * here you will find actual layout for given area
 */

@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutResponse {
    private List<String> accountProximityUUIDs
    private List actions
    private List instantActions

    List<String> getAccountProximityUUIDs() {
        return accountProximityUUIDs ?: []
    }

    void setAccountProximityUUIDs(List<String> accountProximityUUIDs) {
        this.accountProximityUUIDs = accountProximityUUIDs
    }

    List getActions() {
        return actions ?: []
    }

    void setActions(List actions) {
        this.actions = actions
    }

    List getInstantActions() {
        return instantActions ?: []
    }

    void setInstantActions(List instantActions) {
        this.instantActions = instantActions
    }
}
