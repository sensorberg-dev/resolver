package com.sensorberg.front.resolve.producers.els

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.helpers.FileReaderService
import groovy.util.logging.Slf4j
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.Client
import org.elasticsearch.search.SearchHit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * elastic search client producer
 * this one will use configuration in format protocol://<server1>[,<server2>]:<port>/<clusterName>
 * one is injected into elasticsearchConnectionString using @Value elasticsearch.connectionString
 * so to set one set environment variable elasticsearch.connectionString like tcp://host1,host2:9300/my-cluster
 * or use param like --elasticsearch.connectionString when executing syncApplicationRequest
 * there are also other ways to set this variable described in spring documentation
 */
@Slf4j
@Service
class ESClientProducer {

    @Autowired
    FileReaderService fileReader

    @Value('${elasticsearch.connectionString}')
    String elasticsearchConnectionString

    @Autowired
    ESConfig esConfig

    private Client client

    private final ObjectMapper objectMapper = new ObjectMapper()

    @PostConstruct
    public void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        log.info("els connectionString: {}", elasticsearchConnectionString)
        log.info("els index name: {}" , esConfig.getIndexName())

        client = ElsConfigBuilder.buildClient(elasticsearchConnectionString)
        if(client == null) {
            log.error(
                    "els configuration is invalid - please configure elasticsearchConnectionString as http://host:port[,host:port][/clusterName] you can pass this in runtime as argument, env, file. More info http://sometinyurl")
        }
        tryInitIndexes()
        registerObjectHelpers()
    }

    private void tryInitIndexes() {
        try {
            def exists = client.admin().indices().exists(new IndicesExistsRequest(esConfig.getIndexName())).actionGet().exists
            if (!exists) {
                client.admin().indices().prepareCreate(esConfig.getIndexName())
                        .addMapping(esConfig.INDEX.action, fileReader.contentAsString("els/mapping.action.json"))
                        .addMapping(esConfig.INDEX.application, fileReader.contentAsString("els/mapping.application.json"))
                        .addMapping(esConfig.INDEX.beacon, fileReader.contentAsString("els/mapping.beacon.json"))
                        .addMapping(esConfig.INDEX.layoutLog, fileReader.contentAsString("els/mapping.layoutLog.json"))
                        .addMapping(esConfig.INDEX.synchronizationLog, fileReader.contentAsString("els/mapping.synchronizationLog.json"))
                        .execute().actionGet()
                log.info("indexes created")
            } else {
                log.info("indexes found")
            }
        } catch (Exception e) {
            log.warn("cannot create indexes: {}", e.getMessage())
        }
    }

    /**
     * meta programming for object deserialization using FasterXml ObjectMapper
     * @return
     */
    private registerObjectHelpers() {
        SearchHit.metaClass.asObject = { clazz ->
            //objectMapper.readValue(sourceAsString, clazz)
            (sourceAsString != null) ? objectMapper.readValue(sourceAsString, clazz) : null
        }
        GetResponse.metaClass.asObject = { clazz ->
            (sourceAsBytes != null) ? objectMapper.readValue(sourceAsBytes, clazz) : null
        }
    }

    public void forceRecreateIndexes() {
        client.admin().indices().delete(new DeleteIndexRequest(esConfig.getIndexName())).actionGet()
        tryInitIndexes()
    }

    @PreDestroy
    public void closeClient() {
        client?.close()
    }

    @Bean
    public Client getClient() {
        if(client == null) {
            init()
        }
        return client
    }
}
