package com.sensorberg.front.resolve.resources.application

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import com.sensorberg.front.resolve.resources.application.domain.Application
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Created by witek on 19/02/15.
 */
@Service
class ApplicationService implements IsSearchClient {

    /**
     * for later optimization - if company owns more than X beacons we will call it a large one
     * is this case probably it will not be optimal to send entire layout - instead we will try
     * to narrow it to user current position
     */
    @Value('${apiKey.largeCompany}')
    public static int largeCompany

    @Autowired
    ESConfig esConfig

    public boolean isLargeApplication(String apiKey) {
        def response = client.prepareGet(esConfig.getIndexName(), esConfig.INDEX.application, apiKey).execute().actionGet()
        return response.exists && response.fields.beacons.value > largeCompany
    }

    public Application getByApiKey(String apiKey) {
        client.prepareGet(esConfig.getIndexName(), esConfig.INDEX.application, apiKey).get().asObject(Application)
    }

}
