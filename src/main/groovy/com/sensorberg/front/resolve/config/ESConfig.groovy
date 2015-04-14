package com.sensorberg.front.resolve.config

/**
 * elastic search configuration with main index name and types
 */
class ESConfig {
    public static String INDEX_NAME = "beacon_layout"
    public static def INDEX = [
            beacon: "beacon",
            action: "action",
            application: "application",
            syncApplications: "syncApplications",
            synchronizationLog: "synchronizationLog",
            monitoringLog: "monitoringLog",
            layoutLog: "layoutLog"
    ]
}
