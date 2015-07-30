package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.annotation.Resource

/**
 * index resource
 */
@RestController
class IndexResource {

    @Resource
    IndexService indexService

    @Resource
    SynchronizationService synchronizationService

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    def recentLogs(
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "from", required = false, defaultValue = "0") int from
    ) {
        synchronizationService.recentLogs(size, from)
    }

    @RequestMapping(value = "/index", method = RequestMethod.PUT)
    def synchronize(@RequestParam(value = "force", required = false) boolean force) {
        return (!force) ? synchronizationService.synchronize() : synchronizationService.synchronizeForce()
    }

    @RequestMapping(value = "/index", method = RequestMethod.DELETE)
    def reset() {
        indexService.reset()
        return [success: true]
    }

}