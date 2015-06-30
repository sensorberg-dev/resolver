package com.sensorberg.front.resolve.resources.layout.domain

import spock.lang.Specification

class BeaconGeoTest extends Specification {
    def "HasLocation"() {
        setup:
        def beacon = new Beacon()
        when:
        beacon.latitude = lat
        beacon.longitude = lng
        then:
        assert beacon.hasLocation() == result
        where:
        lat         | lng       | result
        null        | null      | false
        52.505640   | 13.429829 | true
        -91         | 0         | false
        0           | -191      | false
        0           | null      | false
    }
}
