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

    def DEFAULT_LAYOUT = [
        accountProximityUUIDs: [
                "7367672374000000ffff0000ffff0000",
                "7367672374000000ffff0000ffff0001",
                "7367672374000000ffff0000ffff0002",
                "7367672374000000ffff0000ffff0003",
                "7367672374000000ffff0000ffff0004",
                "7367672374000000ffff0000ffff0005",
                "7367672374000000ffff0000ffff0006",
                "7367672374000000ffff0000ffff0007",
                "b9407f30f5f8466eaff925556b57fe6d",
                "f7826da64fa24e988024bc5b71e0893e",
                "2f234454cf6d4a0fadf2f4911ba9ffa6",
                "f0018b9b75094c31a9051a27d39c003c",
                "23a01af0232a45189c0e323fb773f5ef"
        ],
        "actions": []
    ]

    @Resource
    LayoutService service

    @RequestMapping(value = "/layout", method = [GET])
    def produceLayout(
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-iid", required = false) String installationId,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-geo", required = false) String geohash,
            @RequestHeader(value = "X-pid", required = false) String pid,
            @RequestHeader(value = "X-qos", required = false) String qos,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
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
        if (rq.method == "GET" && apiKey.equalsIgnoreCase("0000000000000000000000000000000000000000000000000000000000000000")) {
            httpHeaders.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=86400")
            httpHeaders.add("ETag", "${System.currentTimeMillis()}")
            return new ResponseEntity(DEFAULT_LAYOUT, httpHeaders, HttpStatus.OK)
        }

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

    @RequestMapping(value = "/layout", method = [PUT, POST])
    def consumeActivity(
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-iid", required = false) String installationId,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-geo", required = false) String geohash,
            @RequestHeader(value = "X-pid", required = false) String pid,
            @RequestHeader(value = "X-qos", required = false) String qos,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestBody(required = true) LayoutRequestBody activity,
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
        if (service.sendActivity(new LayoutCtx(request: request))){
            return new ResponseEntity(HttpStatus.OK)
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

}
