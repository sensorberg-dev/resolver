package com.sensorberg.front.resolve.scheduled
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import groovy.util.logging.Slf4j
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
/**
 * since syncApplicationRequest is using bulk pulls such action must be scheduled
 */
@Service
@Slf4j
class ScheduledSynchronization {
    @Autowired
    SynchronizationService service

    @Autowired
    ESConfig esConfig

    @Autowired
    Client client

    void synchronize() {
        service.synchronizeForce()
    }

    // Call every 5 minutes
    @Scheduled(fixedDelay = 300000l)
    void deleteSyncData() {

        log.info("Delete Sync Data called.")

        // Delete all beacons/actions from beacon_layout
        DeleteByQueryRequestBuilder requestBuilder = new DeleteByQueryRequestBuilder(client)
                .setIndices(esConfig.getIndexName())
                .setTypes(ESConfig.INDEX.beacon, ESConfig.INDEX.action)
                .setQuery(QueryBuilders.matchAllQuery())

        requestBuilder.get();

        // Call sync force
        service.synchronizeForce()
    }
}
