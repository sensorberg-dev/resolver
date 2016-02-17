package com.sensorberg.front.resolve.resources.settings

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.GET

@RestController
class SettingsResource {

    static int ONE_DAY = 86400
    static int THIRTY_DAYS = ONE_DAY * 30
    static String CACHE_HEADER_THIRTY_DAYS = "no-transform,public,max-age=${THIRTY_DAYS},s-maxage=${THIRTY_DAYS}"

    @RequestMapping(value = "/applications/{apiToken}/settings/{path:.*}", method = GET)
    def settings(@PathVariable(value = "path") String path,
                 @PathVariable(value = "apiToken") String apiToken){
        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .header("Location", "https://connect.sensorberg.com/api/applications/${apiToken}/settings/${path}")
                .header("Cache-Control", CACHE_HEADER_THIRTY_DAYS)
                .build()
    }


}
