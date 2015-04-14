package com.sensorberg.front.resolve.resources

import com.sensorberg.front.resolve.config.ESConfig
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * ping service / provides basic information about application and database counts
 */
@RestController
class Ping {

    @Autowired
    Client client

    @Autowired
    private Environment env


    @RequestMapping("/ping")
    def getInfo() {
        try {
            return [
                    version: this.class.getPackage().getImplementationVersion()
            ] +
                    getCounts(ESConfig.INDEX_NAME, ESConfig.INDEX)
        } catch (Exception e) {
            return [
                    errorType   : e.class.name,
                    errorMessage: e.getMessage()
            ]
        }
    }

    Map getCounts(String index, Map<String, String> types) {
        types.collectEntries { key, value ->
            [
                    "${key}": getCount(index, value)
            ]
        }
    }

    def getCount(String index, String type) {
        try {
            return client.prepareCount(index).setQuery(QueryBuilders.termQuery("_type", type)).execute().actionGet().count
        } catch (Exception e) {
            return e.getMessage()
        }
    }
}
