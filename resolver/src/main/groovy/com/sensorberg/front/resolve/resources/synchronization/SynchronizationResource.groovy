package com.sensorberg.front.resolve.resources.synchronization

import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.annotation.Resource

/**
 * synchronization resource
 */
@RestController
class SynchronizationResource {

    @Resource
    SynchronizationService service

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
     * @param apiKey syncApplicationRequest apiKey to be deleted
     * @return operation status
     */
    @RequestMapping(value = "/synchronizations/{apiKey}", method = RequestMethod.DELETE)
    def delete(@PathVariable(value = "apiKey") String apiKey) {
        service.delete(apiKey)
    }

}
