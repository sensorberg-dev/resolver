package com.sensorberg.front.resolve.config
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
/**
 * elastic search configuration with main index name and types
 */

@Service
class ESConfig {

    public static final long TTL_DAY = 86400000 // 1 day
    public static final long TTL_SYNCHRONIZATION_LOG = TTL_DAY * 14 // 14 days
    public static final long TTL_LOG = TTL_DAY * 30 // 30 days

    public static def INDEX = [
            beacon: "beacon",
            action: "action",
            application: "syncApplicationRequest",
            syncApplications: "syncApplications",
            synchronizationLog: "synchronizationLog",
            monitoringLog: "monitoringLog",
            layoutLog: "layoutLog"
    ]
    public static final int MAX_SEARCH_RESULTS = Integer.MAX_VALUE

    // 1. Try to get from VM env
    // 2. Try to get from application.properties
    @Value('${elasticsearch.indexName}')
    private final String indexName;

    public String getIndexName(){
        return indexName;
    }

    public String getLayoutLogName(){
        return INDEX.layoutLog
    }
}
