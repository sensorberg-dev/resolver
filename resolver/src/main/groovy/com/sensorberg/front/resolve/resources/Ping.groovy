package com.sensorberg.front.resolve.resources
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.service.elasticsearch.EsReaderService
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
/**
 * ping service / provides basic information about syncApplicationRequest and database counts
 */
@RestController
class Ping {

    @Autowired
    Client client

    @Autowired
    private Environment env

    @Autowired
    ESConfig esConfig

    // TEST TEST TEST
    @Autowired
    EsReaderService esReaderService

    @Value('${application.version}')
    String applicationVersion

    @RequestMapping("/ping")
    def getInfo() {

        // only test
        DateTime start = new DateTime();
        DateTime end = new DateTime();
        end.minusYears(2);

        esReaderService.readEntries(start.toDate(), end.toDate());

        try {
            return [
                    version: applicationVersion
            ] +
                    getCounts(esConfig.getIndexName(), esConfig.INDEX)
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
