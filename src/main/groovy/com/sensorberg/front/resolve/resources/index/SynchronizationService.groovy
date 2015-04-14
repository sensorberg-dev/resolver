package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationRequest
import com.sensorberg.front.resolve.resources.index.domain.SyncApplicationValidator
import com.sensorberg.front.resolve.resources.index.domain.SynchronizationLogItem
import com.sensorberg.front.resolve.resources.layout.domain.Action
import com.sensorberg.front.resolve.resources.layout.domain.Beacon
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.client.utils.URLEncodedUtils
import org.springframework.stereotype.Service

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
    SynchronizationLogProvider provider

    JsonSlurper slurper = new JsonSlurper()

    public void synchronize() {
        provider.listSyncApplications().each { sa ->
            // verify SyncApplicationRequest request
            if(!SyncApplicationValidator.isValid(sa)) {
                log.warn("SyncApplicationRequest is not valid [host: {}, apiKey: {}]", sa?.host, sa?.apiKey)
                return
            }
            // read last synchronization date
            def logItem = provider.getLastLogItem(sa) ?: new SynchronizationLogItem(
                    tillVersionId: 0
            )
            // offer application and last synchronization date to synchronization method
            def synchronizationResult = synchronizeApplication(sa, logItem.tillVersionId)
            // log results
            provider.putLogItem(synchronizationResult)
        }
    }

    def SynchronizationLogItem synchronizeApplication(SyncApplicationRequest sa, long currentVersionId) {
        def logItem = new SynchronizationLogItem(
                environment: sa.environment,
                synchronizationDate: new Date()
        )
        try {
            // todo: merge beacons and actions into one synchronization call
            def beaconsSyncResult = synchronizeBeacons(sa, currentVersionId)
            def actionsSyncResult = synchronizeActions(sa, currentVersionId)

            logItem.tillVersionId = beaconsSyncResult.tillVersionId
            logItem.changedItems += beaconsSyncResult.changedItems + actionsSyncResult.changedItems
            logItem.status = true
        } catch (Exception e) {
            logItem.status = false
            logItem.statusDetails = e.getMessage()
        }

        return logItem
    }

    public Collection<SynchronizationLogItem> recentLogs() {
        provider.recentLogs()
    }

    public SyncApplicationRequest addSyncApplication(String token) {
        try {
            URI uri = new URI(new String(token.decodeBase64()))
            Map<String, String> params = URLEncodedUtils.parse(uri, "UTF-8").collectEntries {
                [it.name, it.value]
            }
            return addSyncApplication(
                    new SyncApplicationRequest(
                            host: "${uri.scheme}://${uri.authority}",
                            apiKey: uri.getPath().replaceFirst("/", ""),
                            token: params.get("token")
                    )
            )
        } catch (URISyntaxException e) {
            return null
        }
    }

    public SyncApplicationRequest addSyncApplication(SyncApplicationRequest syncApplication) {
        def result = provider.addSyncApplication(syncApplication)
        synchronize()
        return result
    }

    public List<SyncApplicationRequest> listSyncApplications() {
        return provider.listSyncApplications()
    }

    public boolean delete(String apiKey) {
        return provider.delete(apiKey)
    }

    private SynchronizationLogItem synchronizeBeacons(SyncApplicationRequest sa, long versionId) {
        def result = new SynchronizationLogItem(
                environment: sa.environment,
                synchronizationDate: new Date()
        )
        def http = new HTTPBuilder("${sa.host}/api/synchronizations/${sa.apiKey}/beacons?versionId=${versionId}")
        def processedVersionId
        http.request(Method.GET, ContentType.JSON) {
            headers["X-Auth-Token"] = sa.token
            response.success = { resp, json ->
                // todo: consider passing entire object wrapper into indexBeacons
                processedVersionId = json.tillVersionId as long
                indexService.indexBeacons(json.items as Collection<Beacon>, processedVersionId)
                result.changedItems = json.items.size()
                result.status = true
                result.tillVersionId = processedVersionId
            }

            response.failure = {

            }
        }
        return result
    }

    private SynchronizationLogItem synchronizeActions(SyncApplicationRequest sa, long versionId) {
        def result = new SynchronizationLogItem(
                environment: sa.environment,
                synchronizationDate: new Date()
        )
        def http = new HTTPBuilder("${sa.host}/api/synchronizations/${sa.apiKey}/actions?versionId=${versionId}")
        def processedVersionId
        http.request(Method.GET, ContentType.JSON) {
            headers["X-Auth-Token"] = sa.token
            response.success = { resp, json ->
                processedVersionId = json.tillVersionId as long
                def actions = json.items.collect { item ->
                    // due to legacy date format (represented by long number) we must convert it
                    item.timeframes = item.timeframes?.collect { timeframe ->
                        [
                                start: (timeframe.start != null) ? new Date(timeframe.start) : null,
                                end: (timeframe.end != null) ? new Date(timeframe.end) : null
                        ]
                    }
                    item.deliverAt = (item.deliverAt != null) ? new Date(item.deliverAt) : null
                    return new Action(item)
                }.findAll({it != null})
                indexService.indexActions(actions, processedVersionId)
                indexService.analyzeApplications()
                result.changedItems = json.items.size()
                result.status = true
                result.tillVersionId = processedVersionId
            }
            response.failure = {

            }
        }
        return result
    }

    def asObject(def payload) {
        try {
            return slurper.parseText(payload)
        } catch (ignored) {
            return null
        }
    }

}
