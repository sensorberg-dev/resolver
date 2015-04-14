package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * timeframe object
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class Timeframe {
    Date start
    Date end
}
