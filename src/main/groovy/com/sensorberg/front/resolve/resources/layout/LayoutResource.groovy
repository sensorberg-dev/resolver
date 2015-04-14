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

    @RequestMapping(value = "/layout", method = [PUT, POST])
    def report(@RequestHeader(value = "User-Agent", required = false) String userAgent,
               @RequestHeader(value = "X-iid", required = false) String installationId,
               @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
               @RequestHeader(value = "X-geo", required = false) String geohash,
               @RequestBody(required = true) LayoutRequestBody activity,
               HttpServletRequest rq){
        return new ResponseEntity("not implemented", HttpStatus.METHOD_NOT_ALLOWED)
    }


    @RequestMapping(value = "/layout", method = [GET])
    def layout(
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Device-Id", required = false) String oldInstallationId,
            @RequestHeader(value = "X-iid", required = false) String installationId,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "geo", required = false) String oldGeohash,
            @RequestHeader(value = "X-geo", required = false) String geohash,
            @RequestHeader(value = "pid", required = false) String oldPid,
            @RequestHeader(value = "X-pid", required = false) String pid,
            @RequestHeader(value = "X-qos", required = false) String qos,
            @RequestHeader(value = "qos", required = false) String oldQos,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            HttpServletRequest rq) {

        def request = new LayoutRequest(
                diRaw: userAgent,
                deviceId: installationId ?: oldInstallationId,
                di: UserAgentParser.toDeviceIdentifier(userAgent, installationId ?: oldInstallationId),
                apiKey: apiKey,
                geohash: geohash ?: oldGeohash,
                remoteAddr: forwardedFor ?: rq.remoteAddr,
                qos: qos ?: oldQos,
                pid: pid ?: oldPid
        )

        def ctx = service.layout(new LayoutCtx(request: request))
        def httpHeaders = new HttpHeaders()
        httpHeaders.add("Cache-Control", "no-transform,public,max-age=6000,s-maxage=6000")
        httpHeaders.add("ETag", "${System.currentTimeMillis()}")
        return new ResponseEntity(ctx.response, httpHeaders, HttpStatus.OK)
    }

}
