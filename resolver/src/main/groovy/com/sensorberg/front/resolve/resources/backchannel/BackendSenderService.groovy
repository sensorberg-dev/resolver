package com.sensorberg.front.resolve.resources.backchannel

import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.resources.backchannel.domain.BackchannelResponseWrapper
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import groovy.util.logging.Slf4j
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

import java.util.concurrent.Future

/**
 * backend sender
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

    @Async
    public Future<Void> send(LayoutCtx ctx) throws InterruptedException {
        // todo: should we send ctx when there are no actions?
        if (ctx?.request?.activity?.actions == null || ctx.request.activity.actions.size() == 0) {
            return updateAsDelivered(ctx)
        }
        def backchannelUrl = ctx?.syncApplicationRequest?.backchannelUrl
        // do we have back channel defined?
        if (backchannelUrl == null) {
            return updateAsPrivate(ctx)
        }
        try {
            def response = restTemplate.postForEntity(backchannelUrl, ctx, BackchannelResponseWrapper)
            return updateAsDelivered(ctx, response.getBody())
        } catch (Exception e) {
            log.debug("cannot send event to backend [reason: {}]", e.getMessage())
            return updateLastError(ctx, "exception: $e.message")
        }
    }

    private void updateAsDelivered(LayoutCtx ctx, BackchannelResponseWrapper response = new BackchannelResponseWrapper(actionsResolved: 0)) {
        ctx.reportedBack = [dt: new Date(), success: true, actionsResolved: response.actionsResolved]
        write(ctx);
    }

    private void updateLastError(LayoutCtx ctx, String problem) {
        ctx.reportedBack = [dt: new Date(), success: false, problem: problem]
        write(ctx);
    }

    private void updateAsPrivate(LayoutCtx ctx) {
        ctx.reportedBack =  [dt: new Date(), success: true, type: "private"]
        write(ctx);
    }

    void write(LayoutCtx ctx) {
        client.prepareIndex(ESConfig.INDEX_NAME, ESConfig.INDEX.layoutLog, ctx.id)
                .setSource(mapper.writeValueAsBytes(ctx))
                .setTTL(ESConfig.TTL_LOG)
                .execute().actionGet()
    }
}
