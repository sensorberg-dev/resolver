package com.sensorberg.front.resolve.producers

import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

import javax.annotation.PostConstruct

@Configuration
class RestClientProducer {

    final static int TOTAL_CONNECTIONS = 30
    final static int TOTAL_CONNECTIONS_PER_ROUTE = 20
    final static int CONNECTION_TIMEOUT_MS = 10000
    RestTemplate restTemplate

    @PostConstruct
    void init() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager()
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(CONNECTION_TIMEOUT_MS)
                .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
                .build()
        connectionManager.setMaxTotal(TOTAL_CONNECTIONS)
        connectionManager.setDefaultMaxPerRoute(TOTAL_CONNECTIONS_PER_ROUTE)

        HttpClient defaultHttpClient = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build()

        def httpFactory = new HttpComponentsClientHttpRequestFactory(defaultHttpClient)
        restTemplate = new RestTemplate(httpFactory)
    }

    @Bean
    RestTemplate produce() {
        return restTemplate
    }
}
