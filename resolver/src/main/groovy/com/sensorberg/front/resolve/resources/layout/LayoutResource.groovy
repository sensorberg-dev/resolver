package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequest
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestBody
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.annotation.Resource
import javax.servlet.http.HttpServletRequest

import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * layout resource
 */
@RestController
class LayoutResource {

    @Resource
    LayoutService service

    @RequestMapping(value = "/layout", method = [GET, PUT, POST])
    def layout(
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-iid", required = false) String installationId,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-geo", required = false) String geohash,
            @RequestHeader(value = "X-pid", required = false) String pid,
            @RequestHeader(value = "X-qos", required = false) String qos,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestBody(required = false) LayoutRequestBody activity,
            HttpServletRequest rq) {

        def request = new LayoutRequest(
                diRaw: userAgent,
                deviceId: installationId,
                di: UserAgentParser.toDeviceIdentifier(userAgent, installationId),
                apiKey: apiKey,
                geohash: geohash,
                remoteAddr: forwardedFor ?: rq.remoteAddr,
                activity: activity,
                qos: qos,
                pid: pid,
                etag: ifNoneMatch
        )

        def httpHeaders = new HttpHeaders()
        def ctx = service.layout(new LayoutCtx(request: request))
        httpHeaders.add("X-lid", ctx.id)
        if(ctx.response == null) {
            return new ResponseEntity(httpHeaders, HttpStatus.NO_CONTENT)
        }
        if(ctx.response.currentVersion) {
            return new ResponseEntity(httpHeaders, HttpStatus.NOT_MODIFIED);
        }
        httpHeaders.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=86400")
        httpHeaders.add("ETag", "${System.currentTimeMillis()}")
        return new ResponseEntity(ctx.response, httpHeaders, HttpStatus.OK)
    }

}
