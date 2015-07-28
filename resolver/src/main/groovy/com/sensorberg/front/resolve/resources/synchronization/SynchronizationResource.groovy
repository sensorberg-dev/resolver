package com.sensorberg.front.resolve.resources.synchronization

import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.annotation.Resource

/**
 * synchronization resource
 */
@RestController
class SynchronizationResource {

    @Resource
    SynchronizationService service

    @Resource
    LogP

    /**
     * get list of available synchronizations
     * @return list of synchronizations
     */
    @RequestMapping(value = "/synchronizations", method = RequestMethod.GET)
    def list() {
        service.listSyncApplications()
    }

    /**
     * creates new synchronization endpoint
     * via standard REST call @see SyncApplicationRequest
     * @param syncApplication
     * @return
     */
    @RequestMapping(value = "/synchronizations", method = RequestMethod.POST)
    def add(@RequestBody(required = false) SyncApplicationRequest syncApplication) {
        if(syncApplication != null) {
            return service.addSyncApplication(syncApplication)
        }
        return null
    }

    /**
     * delete synchronization endpoint using syncApplicationRequest apiKey
     * @param synchronizationId syncApplicationRequest synchronizationId to be deleted
     * @return operation status
     */
    @RequestMapping(value = "/synchronizations/{synchronizationId}", method = RequestMethod.DELETE)
    def delete(@PathVariable(value = "synchronizationId") String synchronizationId) {
        service.delete(synchronizationId)
    }


    @RequestMapping(value = "/synchronizations/{synchronizationId}", method = RequestMethod.POST)
    def injectTestData(
            @RequestBody(required = true) SynchronizationResponse testData,
            @RequestParam(required = true) String synchronizationId) {
        SyncApplicationRequest request = new SyncApplicationRequest(){{
            id = synchronizationId;
        }}
        return service.inject(testData, request)

    }

}
