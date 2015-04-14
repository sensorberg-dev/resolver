package com.sensorberg.front.resolve.resources.logs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * logs resource
 */
@RestController
class LogResource {

    @Autowired
    LogService logService

    @RequestMapping(value = "/logs", method = RequestMethod.GET)
    def list() {
        logService.layoutLogs
    }

    @RequestMapping(value = "/logs/{id}", method = RequestMethod.GET)
    def getOne(@PathVariable(value = "id") String id) {
        logService.getLayoutLog(id)
    }
}
