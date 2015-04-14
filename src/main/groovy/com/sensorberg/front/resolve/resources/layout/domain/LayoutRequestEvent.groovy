package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout request event
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutRequestEvent {
    String pid
    Date dt
    int trigger
    String location
}
