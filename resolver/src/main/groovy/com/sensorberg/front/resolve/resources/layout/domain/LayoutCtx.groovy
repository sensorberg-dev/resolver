package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import groovy.transform.AutoClone

import static java.util.Collections.singletonList

/**
 * layout context
 * this one contains request and response for given layout request and will be stored for
 * performance and monitoring in logs
 * you can access last X elements via REST call - see /logs endpoint
 */
@AutoClone
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

    boolean getHasActivity() {
        request?.activity?.actions || request?.activity?.events || request?.activity?.conversions
    }

    List<LayoutCtx> split(int maxCountOfItems) {
        // "this" must not be modified, because it is the response to the mobile client
        LayoutCtx sampleCtx = createCleanClone()

        // if there is no request or no activity, return immediately
        if (request?.activity == null) {
            return singletonList(sampleCtx)
        }

        LayoutRequestBody activity = request.activity

        ItemCounts counts = new ItemCounts(activity, maxCountOfItems)
        List<LayoutCtx> result = new ArrayList<>(64)
        while (counts.countOfHandledItems < counts.countOfAllItems) {
            LayoutCtx currentCtx = addAndGetNewContext(sampleCtx, result, counts)

            splitEventItems(activity.events, counts, currentCtx)
            splitActionItems(activity.actions, counts, currentCtx)
            splitConversionItems(activity.conversions, counts, currentCtx)
        }

        // should be done in test (to find the right maxCountOfItems:
        // azureEventHubService.checkObjectSize(ctx)

        result
    }

    static void splitEventItems(List<LayoutRequestEvent> events, ItemCounts counts, LayoutCtx ctx) {
        if (counts.countOfHandledEvents < counts.countOfEventItems &&
                counts.countOfCurrentItems < counts.maxCountOfItems) {

            int eventsToHandle = Math.min(counts.countOfEventItems - counts.countOfHandledEvents,
                                          counts.maxCountOfItems - counts.countOfCurrentItems)
            ctx.request.activity.events = events.subList(counts.countOfHandledEvents, counts.countOfHandledEvents + eventsToHandle)
            counts.handleEvents(eventsToHandle)
        }
    }

    static void splitActionItems(List<LayoutRequestAction> actions, ItemCounts counts, LayoutCtx ctx) {
        if (counts.countOfHandledActions < counts.countOfActionItems &&
                counts.countOfCurrentItems < counts.maxCountOfItems) {

            int actionsToHandle = Math.min(counts.countOfActionItems - counts.countOfHandledActions,
                    counts.maxCountOfItems - counts.countOfCurrentItems)
            ctx.request.activity.actions = actions.subList(counts.countOfHandledActions, counts.countOfHandledActions + actionsToHandle)
            counts.handleActions(actionsToHandle)
        }
    }

    static void splitConversionItems(List<LayoutRequestConversion> conversionses, ItemCounts counts, LayoutCtx ctx) {
        if (counts.countOfHandledConversions < counts.countOfConversionItems &&
                counts.countOfCurrentItems < counts.maxCountOfItems) {

            int conversionsToHandle = Math.min(counts.countOfConversionItems - counts.countOfHandledConversions,
                    counts.maxCountOfItems - counts.countOfCurrentItems)

            ctx.request.activity.conversions = conversionses.subList(counts.countOfHandledConversions, counts.countOfHandledConversions + conversionsToHandle)
            counts.handleConversions(conversionsToHandle)
        }
    }

    static int listSize(List<?> list) {
        return list?.size() ?: 0
    }

    private static LayoutCtx addAndGetNewContext(LayoutCtx cleanSample, List<LayoutCtx> contexts, ItemCounts counts) {
        LayoutCtx ctx = cleanSample.clone()
        ctx.id = ctx.id + "-" + (contexts.size()+1)
        contexts.add(ctx)
        counts.countOfCurrentItems = 0
        return ctx
    }

    /**
     * Deep clone this context with the following specials:
     * - the response is always null
     * - there are no activities (actions, events or conversions)
     */
    private LayoutCtx createCleanClone() {
        LayoutCtx clone = this.clone()
        clone.response = null;   //LayoutResponse response must be null in each splits
        clone.syncApplicationRequest = null;  // ... and so does this.
        clone.request?.activity?.actions?.clear()
        clone.request?.activity?.events?.clear()
        clone.request?.activity?.conversions?.clear()
        return clone
    }

    private static class ItemCounts {
        int countOfHandledItems
        int countOfEventItems
        int countOfActionItems
        int countOfConversionItems
        int countOfAllItems
        int maxCountOfItems
        int countOfHandledEvents = 0
        int countOfHandledActions = 0
        int countOfHandledConversions = 0
        int countOfCurrentItems = 0

        private ItemCounts(LayoutRequestBody activity, int maxCountOfItems) {
            this.countOfEventItems = listSize(activity.events)
            this.countOfActionItems = listSize(activity.actions)
            this.countOfConversionItems = listSize(activity.conversions)

            this.countOfAllItems = countOfEventItems + countOfActionItems + countOfConversionItems
            this.maxCountOfItems = maxCountOfItems
        }

        private void handleEvents(int count) {
            countOfHandledEvents += count
            countOfHandledItems += count
            countOfCurrentItems += count
        }

        private void handleActions(int count) {
            countOfHandledActions += count
            countOfHandledItems += count
            countOfCurrentItems += count
        }

        private void handleConversions(int count) {
            countOfHandledConversions += count
            countOfHandledItems += count
            countOfCurrentItems += count
        }
    }
}
