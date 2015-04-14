package com.sensorberg.front.resolve.producers.els.domain

/**
 * parsed elastic search configuration
 */
class ElsConfig {
    String clusterName
    List<ElsNode> nodes

    boolean isValid() {
        return nodes?.size() > 0 && nodes.findAll { !it.valid }.size() == 0
    }
}
