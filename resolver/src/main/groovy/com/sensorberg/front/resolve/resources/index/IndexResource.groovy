package com.sensorberg.front.resolve.resources.index
import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import com.sensorberg.front.resolve.service.elasticsearch.EsExportService
import org.elasticsearch.common.joda.time.DateTime
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

    @Resource
    EsExportService esExportService;

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

    @RequestMapping(value = "/exportelstoblob", method = RequestMethod.POST)
    def relocateEntries(@RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to) {

        esExportService.relocateEsEntries((from != null) ? DateTime.parse(from) : null,
                (to != null) ? DateTime.parse(to) : null);
    }

}