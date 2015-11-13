package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.application.ApplicationService
import com.sensorberg.front.resolve.resources.application.domain.Application
import com.sensorberg.front.resolve.resources.backchannel.BackendSenderService
import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import com.sensorberg.front.resolve.resources.index.VersionService
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequest
import com.sensorberg.front.resolve.resources.layout.domain.LayoutResponse
import com.sensorberg.front.resolve.resources.logs.LogService
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * layout service
 */
@Service
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

    LayoutCtx layout(LayoutCtx ctx) {
        def measuredResponse = measureTime({
            computeLayout(ctx)
        })
        LayoutCtx resultCtx = measuredResponse.result
        resultCtx.elapsedTime = measuredResponse.elapsedTime
        backendService.send(resultCtx)
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
        if(application == null) {
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
