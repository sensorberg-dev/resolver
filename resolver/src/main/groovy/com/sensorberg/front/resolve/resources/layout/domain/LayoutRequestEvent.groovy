package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * layout request event
 * per documentation events are only simple entries
 * it is possible that event will be created for beacon that is not part of layout
 * if you want to send information about user activity with layout use actions
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
class LayoutRequestEvent {
    String pid
    Date dt
    int trigger
    String location

    /**
     * for backward compatibility with initial sdk versions
     * safe to remove after may 2015
     */
    @JsonProperty("bid")
    public String setBid(String bid) {
        this.pid = bid
    }

}
