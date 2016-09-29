package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * action is part of layout model
 * layout contains actions and assigned beacons
 * action scheduling is quite verbose and should be replaced by more detailed object model in the future
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class Action implements ActionType, HasVersion {
    String id
    /**
     * action name
     */
    String name
    /**
     * company deep link id - use you company id for reference
     */
    String companyId
    /**
     * action payload as a json object
     * this one probably should be more defined than just "valid json object"
     */
    Map payload

    /**
     * delay action delivery time - this event can be cancelled by entry or exit event
     * so you can create and action that will be delivered to user after 10 minutes in range on beacon
     */
    Integer delayTime
    /**
     * suppress action for given time
     */
    Integer suppressionTime
    /**
     * should we send this action only once to user
     */
    boolean sendOnlyOnce
    /**
     * is action valid for entry event
     */
    boolean triggerEntry
    /**
     * is action valid for exit event
     */
    boolean triggerExit
    /**
     * action triggers
     */
    Integer triggers
    /**
     * deliver action after given time
     */
    Integer deliverAfter
    /**
     * deliver action at fixed date
     */
    Date deliverAt
    /**
     * action activity timeframes
     */
    List<Timeframe> timeframes

    /**
     * list of beacons pids
     */
    List<String> beacons

    /**
     * if of the environment
     */
    String environment

    /**
     * apiKey
     */
    String apiKey
    /**
     * list of apiKeys
     */
    List<String> applicationIds = new ArrayList<>()

    /**
     * is action active
     */
    boolean active

    public void setSuppressionTime(Integer suppressionTime) {
        this.sendOnlyOnce = suppressionTime == 0
        this.suppressionTime = suppressionTime
    }

}
