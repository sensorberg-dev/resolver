package com.sensorberg.front.resolve.resources.logs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * logs resource
 */
@RestController
class LogResource {

    @Autowired
    LogService logService

    @RequestMapping(value = "/logs", method = RequestMethod.GET)
    def list(
            @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @RequestParam(value = "size", required = false, defaultValue = "100") Integer size,
            @RequestParam(value = "slow", required = false, defaultValue = "0") Integer slow
    ) {
        logService.getLayoutLogs(from, size, slow)
    }

    @RequestMapping(value = "/logs", method = RequestMethod.DELETE)
    def delete() {
        logService.deleteAll()
        return true
    }

    @RequestMapping(value = "/logs/{id}", method = RequestMethod.GET)
    def getOne(@PathVariable(value = "id") String id) {
        logService.getLayoutLog(id)
    }
}
