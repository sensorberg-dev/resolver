package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutAction {
    String eid
    Integer trigger
    Integer delay
    Date deliverAt
    List<String> beacons
    Integer supressionTime
    Integer suppressionTime
    Map content
    Integer type
    List<Timeframe> timeframes
    boolean sendOnlyOnce
    String typeString
    @JsonIgnore
    Long version
}
