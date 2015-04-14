package com.sensorberg.front.resolve.producers.els.domain

/**
 * elastic search node
 */
class ElsNode {
    String host
    int port

    boolean isValid() {
        return !host?.isEmpty() && port > 0 && port <= 65535
    }
}
