package com.sensorberg.front.resolve.resources.admin
import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.config.ESConfig
import com.sensorberg.front.resolve.helpers.ScriptProvider
import com.sensorberg.front.resolve.resources.application.ApplicationService
import com.sensorberg.front.resolve.resources.application.domain.Application
import com.sensorberg.front.resolve.resources.backchannel.BackendSenderService
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.resources.synchronization.SynchronizationService
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import java.lang.reflect.Method

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT
/**
 * Created by falkorichter on 03.09.15.
 */
@RestController
class AdminResource {

    @Autowired
    Client client

    @Autowired
    BackendSenderService backendService

    @Autowired
    ObjectMapper mapper

    @Autowired
    ApplicationService applicationService

    @Autowired
    SynchronizationService synchronizationService

    @Autowired
    ScriptProvider scriptProvider

    /**
     * @return all failed backchannel requests exluding the response
     */
    @RequestMapping(value = "admin/failedRequests/", method = GET)
    def failedRequests (
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "from", required = false, defaultValue = "0" ) Integer from) {

        def hits = client.prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.layoutLog)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("reportedBack.success", false)))
                .setSize(size)
                .setFrom(from)
                .setFetchSource(null ,"response")
                .get();

        return new ResponseEntity([
                totalCount : hits.hits.totalHits,
                count: size,
                from : from,
                hits : hits.hits.hits.collect({ it.getSource() })
        ], HttpStatus.OK)
    }

    @RequestMapping(value = "admin/failedRequests/", method = PUT)
    def retryFailedRequests(
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "from", required = false, defaultValue = "0" ) Integer from) {

        def hits = client.prepareSearch(ESConfig.INDEX_NAME)
                .setTypes(ESConfig.INDEX.layoutLog)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("reportedBack.success", false)))
                .setFetchSource(null ,"response")
                .setSize(size)
                .setFrom(from)
                .get().getHits()
                .getHits().collect { hit ->
            hit.getSourceAsString();
        };

        hits.collect() { ctxSource ->
            LayoutCtx ctx = mapper.readValue(ctxSource, LayoutCtx.class)
            backendService.sendSync(ctx);
            return ctx;
        }
        return new ResponseEntity(hits, HttpStatus.OK);
    }

    @RequestMapping(value = "test", method = GET)
    def test(){
        return new ResponseEntity(scriptProvider.runScript(ScriptProvider.fileNames.layoutLogReportedBackHistory), HttpStatus.OK);
    }
}
