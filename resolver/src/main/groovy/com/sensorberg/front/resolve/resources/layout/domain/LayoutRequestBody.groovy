package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * layout request body
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutRequestBody {
    Date deviceTimestamp
    List<LayoutRequestEvent> events
    List<LayoutRequestAction> actions
        List<LayoutRequestConversions> conversions
}
