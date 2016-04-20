package com.sensorberg.front.resolve.health

import com.sensorberg.front.resolve.service.AzureBlobStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * spring boot health indicator for Azure event hub
 */
@Component
class AzureBlobStoreHealthService implements HealthIndicator {

    @Autowired
    AzureBlobStorageService azureBlobStorageService

    @Override
    Health health() {
        return azureBlobStorageService.getHealth();
    }
}
