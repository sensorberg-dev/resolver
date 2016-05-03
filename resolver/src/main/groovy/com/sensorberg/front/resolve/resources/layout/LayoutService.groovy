package com.sensorberg.front.resolve.resources.layout
import com.sensorberg.front.resolve.resources.application.ApplicationService
import com.sensorberg.front.resolve.resources.application.domain.Application
import com.sensorberg.front.resolve.resources.backchannel.BackendSenderService
import com.sensorberg.front.resolve.resources.index.VersionService
import com.sensorberg.front.resolve.resources.layout.domain.*
import com.sensorberg.front.resolve.resources.logs.LogService
import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import com.sensorberg.front.resolve.service.AzureEventHubService
import com.sensorberg.groovy.helper.Looper
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
/**
 * layout service
 */
@Service
@Slf4j
class LayoutService {

    @Autowired
    LayoutHandler handler

    @Autowired
    LogService logService

    @Autowired
    ApplicationService applicationService

    @Autowired
    SynchronizationService synchronizationService

    @Autowired
    BackendSenderService backendService

    @Autowired
    VersionService versionService

    @Autowired
    AzureEventHubService azureEventHubService

    /**
     * Amount of splitting for lists in oversize messages.
     */
    private int splitStep = 500;

    LayoutCtx layout(LayoutCtx ctx) {
        def measuredResponse = measureTime({
            computeLayout(ctx)
        })
        LayoutCtx resultCtx = measuredResponse.result
        resultCtx.elapsedTime = measuredResponse.elapsedTime



        // Do not process meaningless data
        // Check if we have a request and activities
        if (ctx.hasEventsOrActions) {
                //log to elasticsearch
                logService.log(ctx)

                //async
                // Write to azure event hub
                // Check message size
                if (azureEventHubService.checkObjectSize(ctx)) {
                    azureEventHubService.sendObjectMessage(ctx);
                } else {
                    // Message ist to large, split activity Actions/Event in 1000 Steps
                    splitLayoutCtxAndWriteToAzure(ctx);
                }

                //send to main backend (backchannel)
                backendService.send(resultCtx)
                // end of async

        }
        return resultCtx
    }

    /**
     * In case the context is to large to be send in whole, we split the message
     * by splitting the event list and the action list of the request.
     * I assumed, that the response part will never be to large.
     * The split step 500 is guessed, because we can not be sure how
     * large on entry in a list actual is, because it depends on the payload. (String size).
     * This is written in java, because the new resolver will be written in java as well
     * The method is public for testing.
     */
    void splitLayoutCtxAndWriteToAzure(LayoutCtx originalCtx) {

        if (null == originalCtx || null == originalCtx.getRequest() || null == originalCtx.getRequest().getActivity()) {
            return;
        }

        // Save original lists
        List<LayoutRequestEvent> originalRequestEventList = originalCtx.getRequest().getActivity().getEvents();
        List<LayoutRequestAction> originalRequestActionList = originalCtx.getRequest().getActivity().getActions();

        int originalEventSize = 0
        if (null != originalRequestEventList) {
            originalEventSize = originalRequestEventList.size();
        }

        int originalActionSize = 0;
        if (null != originalRequestActionList) {
            originalActionSize = originalRequestActionList.size();
        }

        List<LayoutRequestEvent> splitListEvent = new ArrayList<>();
        List<LayoutRequestAction> splitListAction = new ArrayList<>();

        int startPosition = 0;

        boolean eventsFinished = false;
        boolean actionFinished = false;

        int count = 1;

        Looper.loop {

            int endPositionEventList = startPosition + splitStep;
            int endPositionActionList = startPosition + splitStep;

            // Check for end of events
            if (endPositionEventList > originalEventSize) {
                endPositionEventList = originalEventSize;
                eventsFinished = true;
            }

            // Check for end of actions
            if (endPositionActionList > originalActionSize) {
                endPositionActionList = originalActionSize;
                actionFinished = true;
            }

            splitListEvent.clear();
            splitListAction.clear();

            if (!eventsFinished) {
                log.info("events start {} end {}", startPosition, endPositionEventList);
                // take a sub list from the original list
                splitListEvent.addAll(originalRequestEventList.subList(startPosition, endPositionEventList));
            }

            if (!actionFinished) {
                log.info("action start {} end {}", startPosition, endPositionEventList);
                // take a sub list from the original list
                splitListAction.addAll(originalRequestActionList.subList(startPosition, endPositionActionList));
            }

            // Set lists to original object
            originalCtx.getRequest().getActivity().setEvents(splitListEvent);
            originalCtx.getRequest().getActivity().setActions(splitListAction);

            // send message
            count++;
            azureEventHubService.sendObjectMessage(originalCtx);

            startPosition += splitStep;

        }.until {
            eventsFinished && actionFinished;
        }
        log.info("Count {}", count)
    }

    private LayoutCtx computeLayout(LayoutCtx ctx) {
        if (!isRequestValid(ctx.request)) {
            ctx.response = new LayoutResponse()
            return ctx
        }
        if (versionService.hasCurrentVersion(ctx.request.apiKey, ctx.request.etag)) {
            ctx.response = new LayoutResponse(currentVersion: true)
            return ctx
        }

        Application application = applicationService.getByApiKey(ctx.request.apiKey)
        if (application == null) {
            ctx.response = null
            return ctx
        }
        if (application?.environment != null) {
            ctx.syncApplicationRequest = synchronizationService.getById(application.environment)
        }

        return isLayoutForBeacon(ctx.request) ?
                handler.getForBeacon(ctx) :
                handler.get(ctx)
    }

    def measureTime = { def closure ->
        def start = System.currentTimeMillis()
        def result = closure.call()
        return [
                result     : result,
                elapsedTime: System.currentTimeMillis() - start
        ]
    }

    private static def isRequestValid(LayoutRequest request) {
        return StringUtils.isNotEmpty(request?.apiKey)
    }

    private static def isLayoutForBeacon = { LayoutRequest request ->
        StringUtils.trimToNull(request.pid) != null
    }

}
