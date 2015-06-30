package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationValidator
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationLogItem
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationResponse
import groovy.util.logging.Slf4j
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import javax.annotation.Resource

/**
 * synchronization service
 * here you can add and manipulate synchronizations
 */
@Slf4j
@Service
class SynchronizationService implements IsSearchClient {

    @Resource
    IndexService indexService

    @Resource
    SynchronizationLogProvider logProvider

    @Autowired
    VersionService versionService

    @Autowired
    RestTemplate restTemplate

    /**
     * sync beacons and actions with endpoints configured in /synchronizations
     */
    public void synchronize() {
        logProvider.listSynchronizations().each { syncDefinition ->
            // verify synchronization definition
            if(!SyncApplicationValidator.isValid(syncDefinition)) {
                log.warn("SyncApplicationRequest is not valid [id: {}, url: {}]", syncDefinition?.id, syncDefinition?.url)
                return
            }
            // read last synchronization date
            def lastSyncLogItem = logProvider.getLastLogItem(syncDefinition)
            // offer application and last synchronization date to synchronization method
            def synchronizationResult = synchronizeAll(syncDefinition, lastSyncLogItem.tillVersionId)
            // log results
            logProvider.putLogItem(synchronizationResult)
            versionService.put(syncDefinition.id, lastSyncLogItem.tillVersionId)
        }
    }

    public void synchronizeForce() {
        logProvider.listSynchronizations().each { syncDefinition ->
            synchronizeAll(syncDefinition)
        }
    }

    def SynchronizationLogItem synchronizeAll(SyncApplicationRequest sa, long currentVersionId = 0) {
        def logItem = new SynchronizationLogItem(
                synchronizationId: sa.id,
                synchronizationDate: new Date()
        )
        try {
            def all = sync(sa, currentVersionId)
            def beaconResult = indexService.indexBeacons(all.beacons, sa, all.tillVersionId)
            def actionResult = indexService.indexActions(all.actions, all.tillVersionId)
            // todo: low priority analyze only changed applications
            indexService.analyzeApplications()

            logItem.tillVersionId = all.tillVersionId
            logItem.changedItems += all.beacons.size() + all.actions.size()
            logItem.status = beaconResult && actionResult


        } catch (Exception e) {
            logItem.status = false
            logItem.statusDetails = e.getMessage()
        }

        return logItem
    }

    public Collection<SynchronizationLogItem> recentLogs(int size, int from) {
        logProvider.recentLogs(size, from)
    }

    public SyncApplicationRequest addSyncApplication(SyncApplicationRequest syncApplication) {
        if(!SyncApplicationValidator.isValid(syncApplication)) {
            throw new RuntimeException("request is not valid")
        }
        def result = logProvider.saveSyncApplication(syncApplication)
        synchronize()
        return result
    }

    public List<SyncApplicationRequest> listSyncApplications() {
        return logProvider.listSynchronizations()
    }

    public SyncApplicationRequest getById(String id) {
        return logProvider.getById(id)
    }

    public boolean delete(String apiKey) {
        return logProvider.delete(apiKey)
    }

    private SynchronizationResponse sync(SyncApplicationRequest sa, long versionId) {
        URI uri = new URI(sa.url)
        def queryPart = URLEncodedUtils.parse(uri, "UTF-8")
        queryPart.add(new BasicNameValuePair("versionId", "$versionId"))
        def queryString = URLEncodedUtils.format(queryPart, "UTF-8")

        def response = restTemplate.getForObject(
                "$uri.scheme://$uri.authority$uri.path?$queryString",
                SynchronizationResponse.class)
        return response
    }

}
