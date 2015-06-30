package com.sensorberg.front.resolve.health

import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.producers.els.domain.IsSearchClient
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus
import org.elasticsearch.common.unit.TimeValue
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * spring boot health indicator for elastic search database
 */
@Component
class ElasticSearchHealth implements HealthIndicator, IsSearchClient {

    private static final int SHORT_TIMEOUT = 1;

    @Override
    Health health() {
        try {
            def status = client.admin().cluster().health(
                    new ClusterHealthRequest(ESConfig.INDEX_NAME)
                            .timeout(TimeValue.timeValueSeconds(SHORT_TIMEOUT)
                    ).waitForYellowStatus()).actionGet().status
            return (status == ClusterHealthStatus.RED) ? Health.down().build() : Health.up().build()
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
