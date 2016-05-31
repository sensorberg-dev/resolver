package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout request action
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutRequestAction {
    String id
    String eid
    String pid
    Date dt
    String location
    int trigger
}
