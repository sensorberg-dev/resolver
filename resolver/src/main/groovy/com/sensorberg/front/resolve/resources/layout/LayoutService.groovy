package com.sensorberg.front.resolve.resources.layout
import com.sensorberg.front.resolve.resources.application.ApplicationService
import com.sensorberg.front.resolve.resources.application.domain.Application
import com.sensorberg.front.resolve.resources.backchannel.BackendSenderService
import com.sensorberg.front.resolve.resources.index.VersionService
import com.sensorberg.front.resolve.resources.layout.domain.*
//import com.sensorberg.front.resolve.resources.logs.LogService
import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import com.sensorberg.front.resolve.service.AzureEventHubService
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

//    @Autowired
//    LogService logService

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
    public static final int SPLIT_SIZE = 1200;

    LayoutCtx layout(LayoutCtx ctx) {
        def measuredResponse = measureTime({
            computeLayout(ctx)
        })
        LayoutCtx resultCtx = measuredResponse.result
        resultCtx.elapsedTime = measuredResponse.elapsedTime

        // Do not process meaningless data
        // Check if we have a request and activities
        if (ctx.hasActivity) {
                //log to elasticsearch
//                logService.log(ctx)

                // Message ist to large, split activity Actions/Event in 1000 Steps
                ctx.split(SPLIT_SIZE).each {
                    azureEventHubService.sendAsyncObjectMessage(it)
                }

                //send to main backend (backchannel)
                backendService.send(resultCtx)
                // end of async

        }
        return resultCtx
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
