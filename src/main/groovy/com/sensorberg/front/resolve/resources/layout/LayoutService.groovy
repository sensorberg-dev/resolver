package com.sensorberg.front.resolve.resources.layout

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

    LayoutCtx layout(LayoutCtx ctx) {
        if (!isRequestValid(ctx.request)) {
            ctx.response = new LayoutResponse()
            logService.log(ctx)
            return ctx
        }

        def measuredResponse = measureTime({
            isLayoutForBeacon(ctx.request) ?
                    handler.getForBeacon(ctx) :
                    handler.get(ctx)
        })
        ctx = measuredResponse.result
        ctx.elapsedTime = measuredResponse.elapsedTime
        logService.log(ctx)
        return ctx
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
