package com.sensorberg.front.resolve.producers.els.domain

import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired

/**
 * elastic search client trait
 */
trait IsSearchClient {
    @Autowired
    Client client
}