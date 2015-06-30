package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest

/**
 * layout context
 * this one contains request and response for given layout request and will be stored for
 * performance and monitoring in logs
 * you can access last X elements via REST call - see /logs endpoint
 */
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class LayoutCtx {

    String id
    Date eventDate
    long elapsedTime
    LayoutRequest request
    LayoutResponse response
    SyncApplicationRequest syncApplicationRequest
    Date reportedBack

    public LayoutCtx() {
        id = UUID.randomUUID()
        eventDate = new Date()
    }

}
