package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import org.apache.commons.lang3.StringUtils
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
     * there are two ways to synchronize:
     * 1. via token "https://<server>:<port>/<apiKey>".bytes.encodeBase64().toString()
     * 2. via standard REST call @see SyncApplicationRequest
     * @param token
     * @param syncApplication
     * @return
     */
    @RequestMapping(value = "/synchronizations", method = RequestMethod.POST)
    def add(
            @RequestParam(value = "token", required = false) String token,
            @RequestBody(required = false) SyncApplicationRequest syncApplication
    ) {
        if(StringUtils.isNotEmpty(token)) {
            return service.addSyncApplication(token)
        } else if(syncApplication != null) {
            return service.addSyncApplication(syncApplication)
        }
        return null
    }

    /**
     * delete synchronization endpoint using application apiKey
     * @param apiKey application apiKey to be deleted
     * @return operation status
     */
    @RequestMapping(value = "/synchronizations/{apiKey}", method = RequestMethod.DELETE)
    def delete(@PathVariable(value = "apiKey") String apiKey) {
        service.delete(apiKey)
    }

}
