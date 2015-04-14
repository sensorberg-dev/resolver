package com.sensorberg.front.resolve.resources.layout.domain

import spock.lang.Specification

/**
 * Created by witek on 19/02/15.
 */
class ActionTypeTest extends Specification {
    def "TypeString"() {
        expect:
        assert new Action(type: type).typeString() == expected
        where:
        type | expected
        1    | "notification"
        2    | "website"
        3    | "function"
        4    | "coupon"
        5    | "vibrate"
        9    | "custom"
        11   | "image"
        12   | "video"
        13   | "audio"
        31   | "mailing"
        999  | "999"
        null | null

    }
}
