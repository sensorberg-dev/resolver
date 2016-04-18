package com.sensorberg.front.resolve.health

import com.sensorberg.front.resolve.service.AzureEventHubService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

/**
 * Created by falkorichter on 18/04/16.
 */
class AzureHealthService implements HealthIndicator {

    @Autowired
    AzureEventHubService azureEventHubService

    @Override
    Health health() {
        return azureEventHubService.isConnectionAlive();
    }
}
