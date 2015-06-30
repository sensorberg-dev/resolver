package com.sensorberg.front.resolve.helpers.aspects

import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.boot.actuate.metrics.repository.MetricRepository
import org.springframework.stereotype.Component

/**
 * WIP for service performance monitoring
 */
@Slf4j
/* monitoring WIP
@Aspect
@Component
 */
class PerformanceLogger {

    @Autowired
    MetricRepository metricRepository

    @Around("@annotation(com.sensorberg.front.resolve.helpers.aspects.Monitored)")
    public def logServiceAccess(ProceedingJoinPoint request) {
        def start = System.currentTimeMillis()
        try {
            def result = request.proceed()
            metricRepository.set(new Metric<Number>("${request.signature.declaringTypeName}.${request.signature.name}", System.currentTimeMillis() - start))
            return result
        } catch (Exception e) {
            metricRepository.set(new Metric<Number>("${request.signature.declaringTypeName}.${request.signature.name}.exception", System.currentTimeMillis() - start))
            throw e
        }
    }
}
