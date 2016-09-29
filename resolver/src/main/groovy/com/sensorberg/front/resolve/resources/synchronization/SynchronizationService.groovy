package com.sensorberg.front.resolve.resources.synchronization

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.index.IndexService
import com.sensorberg.front.resolve.resources.index.VersionService
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationValidator
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationLogItem
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationResponse
import groovy.util.logging.Slf4j
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
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

    @Autowired
    ESConfig esConfig

    @Autowired
    Client client


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
            synchronizationResult.setStatusDetails("source: synchronize")
            logProvider.putLogItem(synchronizationResult)
            versionService.put(syncDefinition.id, synchronizationResult.tillVersionId)
        }
    }

    public void synchronizeForce() {

        log.info("synchronizeForce called.")



        logProvider.listSynchronizations().each { syncDefinition ->
            def synchronizationResult = synchronizeAll(syncDefinition)
            // log results
            synchronizationResult.setStatusDetails("source: synchronizeForce")
            logProvider.putLogItem(synchronizationResult)
            versionService.put(syncDefinition.id, synchronizationResult.tillVersionId)
        }
    }

    /**
     * Main service method for synchronization.
     * @param sa
     * @param currentVersionId
     * @return
     */
    def SynchronizationLogItem synchronizeAll(SyncApplicationRequest sa, long currentVersionId = 0) {

        log.debug("synchronizeAll called for Sync ID:", sa.id)

        def logItem = new SynchronizationLogItem(
                synchronizationId: sa.id,
                synchronizationDate: new Date()
        )
        try {
            // Call backend and get a sync response
            def syncResponse = sync(sa, currentVersionId)
            // Delete all beacons/actions from beacon_layout corresponding to the current SyncApplicationRequest
            DeleteByQueryRequestBuilder requestBuilder = new DeleteByQueryRequestBuilder(client)
                    .setIndices(esConfig.getIndexName())
                    .setTypes(ESConfig.INDEX.beacon, ESConfig.INDEX.action)
                    .setQuery(QueryBuilders.matchQuery("environment", sa.id))
            requestBuilder.get();

            // Index Beacons, Actions and analyze Applications
            processSyncResponse(syncResponse,logItem, sa)

        } catch (Exception e) {

            log.error("synchronizeAll failed.", e)

            logItem.status = false
            logItem.statusDetails = "${e.getClass().name} ${e.getMessage()}\n"

            e.getStackTrace().each { logItem.statusDetails +=  "${it.fileName} ${it.lineNumber} ${it.methodName}\n"}
        }

        logItem.duration =  new Date().getTime() - logItem.synchronizationDate.getTime();

        log.info("synchronizeAll finished.")

        return logItem
    }

    /**
     * inject a sync response to test synchronization.
     * @param syncResponse
     * @param sa
     * @return
     */
    public SynchronizationLogItem syncWithResponse(SynchronizationResponse syncResponse, SyncApplicationRequest sa){
        def logItem = new SynchronizationLogItem(
                synchronizationId: sa.id,
                synchronizationDate: new Date()
        )
        try {
            // Index Beacons, Actions and analyze Applications
            processSyncResponse(syncResponse,logItem, sa)

        } catch (Exception e) {
            logItem.status = false
            logItem.statusDetails = "${e.getClass().name} ${e.getMessage()}\n"

            e.getStackTrace().each { logItem.statusDetails +=  "${it.fileName} ${it.lineNumber} ${it.methodName}\n"}
        }

        logItem.duration =  new Date().getTime() - logItem.synchronizationDate.getTime();
        logProvider.putLogItem(logItem)
        return logItem
    }

    /**
     *
     * @param synchronizationResponse
     */
    private void processSyncResponse(final SynchronizationResponse syncResponse,
                                     final SynchronizationLogItem logItem,
                                     final SyncApplicationRequest sa) {

        def beaconResult = indexService.indexBeacons(syncResponse.beacons, sa, syncResponse.tillVersionId)
        def actionResult = indexService.indexActions(syncResponse.actions, sa, syncResponse.tillVersionId)
        // todo: low priority analyze only changed applications
        indexService.analyzeApplications()

        logItem.tillVersionId = syncResponse.tillVersionId
        logItem.changedItems += syncResponse.beacons.size() + syncResponse.actions.size()
        logItem.status = beaconResult && actionResult
    }

    public Collection<SynchronizationLogItem> recentLogs(int size, int from) {
        logProvider.recentLogs(size, from)
    }

    public SyncApplicationRequest addSyncApplication(SyncApplicationRequest syncApplication) {
        if(!SyncApplicationValidator.isValid(syncApplication)) {

            // TODO: Generate new exception
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

    public boolean delete(String synchronizationId) {
        return logProvider.delete(synchronizationId)
    }

    /**
     * Call the backend to get a sync response for the given ApplicationRequest.
     * @param sa
     * @param versionId
     * @return
     */
    private SynchronizationResponse sync(SyncApplicationRequest sa, long versionId) {

        log.debug("Calling Backend with ApplicationRequest: "  + sa.toString())

        URI uri = new URI(sa.url)

        def queryPart = []
        queryPart.addAll(URLEncodedUtils.parse(uri, "UTF-8"))
        queryPart.add(new BasicNameValuePair("versionId", "$versionId"))

        def queryString = URLEncodedUtils.format(queryPart, "UTF-8")

        def response = restTemplate.getForObject(
                "$uri.scheme://$uri.authority$uri.path?$queryString",
                SynchronizationResponse.class)

        return response
    }
}
