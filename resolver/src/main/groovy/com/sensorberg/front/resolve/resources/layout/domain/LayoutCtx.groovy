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

    boolean getHasEventsOrActions() {
        request?.activity?.actions?.empty == false ||
                request?.activity?.events?.empty == false
    }

    List<LayoutCtx> split(int countOfItems){

        "this" must not be modified, because it is the response to the mobile client

        //split these
        originalCtx.getRequest().getActivity().setEvents(splitListEvent);
        originalCtx.getRequest().getActivity().setActions(splitListAction);

        //LayoutResponse response must be null in each splits

        originalCtx.setId(originalUUID+ "-" + count);

        azureEventHubService.checkObjectSize(ctx)

    }
}
