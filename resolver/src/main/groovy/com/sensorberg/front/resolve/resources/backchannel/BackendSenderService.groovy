package com.sensorberg.front.resolve.resources.backchannel
import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.resources.backchannel.domain.BackchannelResponseWrapper
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import groovy.util.logging.Slf4j
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import java.util.concurrent.Future
/**
 * backend messageProducer
 */
@Slf4j
@Service
class BackendSenderService {

    @Autowired
    Client client

    @Autowired
    ObjectMapper mapper

    @Autowired
    RestTemplate restTemplate

    @Autowired
    ESConfig esConfig

    @Async
    public Future<Void> send(LayoutCtx ctx) throws InterruptedException {
        if (!ctx.hasActivity){
            log.error("trying to send no actions, events or conversions. This should be handled before calling this method")
            return
        }
        def backchannelUrl = ctx?.syncApplicationRequest?.backchannelUrl
        // do we have back channel defined?
        if (backchannelUrl == null) {
            return updateAsPrivate(ctx)
        }
        long startTime = System.currentTimeMillis()
        try {
            def response = restTemplate.postForEntity(backchannelUrl, ctx, BackchannelResponseWrapper)
            return updateAsDelivered(ctx, response.getBody())
        } catch (Exception e) {
            long timeSpent = System.currentTimeMillis()-startTime;
            log.error("cannot send event to backend took ${timeSpent}ms", e)
            return updateLastError(ctx, "exception: ${e.message}, took ${timeSpent}ms")
        }
    }

    private void updateAsDelivered(LayoutCtx ctx, BackchannelResponseWrapper response = new BackchannelResponseWrapper(actionsResolved: 0)) {
        client.update(new UpdateRequest(esConfig.getIndexName(), esConfig.INDEX.layoutLog, ctx.id).doc(
                reportedBack: [dt: new Date(), success: true, actionsResolved: response.actionsResolved]
        )).get()
    }

    private void updateLastError(LayoutCtx ctx, String problem) {
        client.update(new UpdateRequest(esConfig.getIndexName(), esConfig.INDEX.layoutLog, ctx.id).doc(
                reportedBack: [dt: new Date(), success: false, problem: problem]
        )).get()
    }

    private void updateAsPrivate(LayoutCtx ctx) {
        client.update(new UpdateRequest(esConfig.getIndexName(), esConfig.INDEX.layoutLog, ctx.id).doc(
                reportedBack: [dt: new Date(), success: true, type: "private"]
        )).get()
    }
}
