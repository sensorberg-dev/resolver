package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout response
 * here you will find actual layout for given area
 */

@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutResponse {
    private List<String> accountProximityUUIDs
    private List<LayoutAction> actions
    private List instantActions
    private boolean currentVersion

    List<String> getAccountProximityUUIDs() {
        return accountProximityUUIDs ?: []
    }

    void setAccountProximityUUIDs(List<String> accountProximityUUIDs) {
        this.accountProximityUUIDs = accountProximityUUIDs
    }

    List<LayoutAction> getActions() {
        return actions ?: []
    }

    void setActions(List<LayoutAction> actions) {
        this.actions = actions
    }

    List getInstantActions() {
        return instantActions ?: []
    }

    void setInstantActions(List instantActions) {
        this.instantActions = instantActions
    }

    boolean getCurrentVersion() {
        return currentVersion
    }

    boolean isCurrentVersion() {
        return currentVersion
    }

    void setCurrentVersion(boolean hasCurrentVersion) {
        this.currentVersion = hasCurrentVersion
    }
}
