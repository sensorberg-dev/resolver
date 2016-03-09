package com.sensorberg.resource;

import javax.annotation.Resource;

import org.elasticsearch.common.joda.time.DateTime;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sensorberg.front.resolve.service.elasticsearch.EsExportService;

/**
 * Created by Andreas Dörner on 07.03.16.
 */
@RestController
public class ElsMigrationResource {

    @Resource
    EsExportService esExportService;

    @RequestMapping(value = "/exportelstoblob", method = RequestMethod.POST)
    public void relocateEntries(@RequestParam("from") String from, @RequestParam("to") String to) {

        esExportService.relocateEsEntries((from != null) ? DateTime.parse(from) : null,
                (to != null) ? DateTime.parse(to) : null);
    }
}







