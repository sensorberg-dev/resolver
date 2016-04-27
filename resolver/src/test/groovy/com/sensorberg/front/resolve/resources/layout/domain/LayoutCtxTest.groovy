package com.sensorberg.front.resolve.resources.layout.domain

import spock.lang.Specification

class LayoutCtxTest extends Specification {

    def "with no request"(){
        setup:
        def tested = new LayoutCtx();
        expect:
        assert tested.hasEventsOrActions == false
    }

    def "with empty list request"(){
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody (
                                actions: [],
                                events: []
                        )
                )
        )
        expect:
        assert !tested.hasEventsOrActions
    }

    def "with filled list request"(){
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody (
                                actions: [ new LayoutRequestAction() ],
                                events: [ new LayoutRequestEvent()]
                        )
                )
        )
        expect:
        assert tested.hasEventsOrActions
    }

    def "with filled only actions request"(){
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody (
                                actions: [ new LayoutRequestAction() ]
                        )
                )
        )
        expect:
        assert tested.hasEventsOrActions
    }

    def "with empty request"(){
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                )
        )
        expect:
        assert !tested.hasEventsOrActions
    }
}
