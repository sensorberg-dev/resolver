package com.sensorberg.front.resolve.resources.index

import com.sensorberg.front.resolve.resources.layout.domain.Beacon
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/synchronizations/{apiKey}/beacons", produces = "application/json")
public class SynchronizationBeaconResource {

    /*@Autowired
    BeaconService beaconService*/

    @RequestMapping(method = RequestMethod.GET)
    def getBeacons(@PathVariable(value = "apiKey") String apiKey) {
        new ResponseEntity(new Beacon(), HttpStatus.OK)
    }

    @RequestMapping(method = RequestMethod.POST)
    def addBeacon(@PathVariable(value = "apiKey") String apiKey,
                   @RequestBody Beacon beacon) {
        new ResponseEntity(new Beacon(), HttpStatus.OK)
    }
}
