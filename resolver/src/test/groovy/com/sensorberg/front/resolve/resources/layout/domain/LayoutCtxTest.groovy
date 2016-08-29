package com.sensorberg.front.resolve.resources.layout.domain

import com.sensorberg.front.resolve.service.AzureEventHubService
import spock.lang.Specification

import static java.lang.Boolean.TRUE
import static java.util.Collections.emptyList

class LayoutCtxTest extends Specification {

    def "with no request"(){
        setup:
        def tested = new LayoutCtx();
        expect:
        assert !tested.hasEventsOrActions
    }

    def "with empty list request"(){
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody (
                                actions: [],
                                events: [],
                                conversions: [],
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

    def "only with conversions"(){
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody (
                                conversions: [ new LayoutRequestConversion() ]
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

    def "split no request"() {
        setup:
        def tested = new LayoutCtx(
        )
        when:
        def actual = tested.split(3)
        then:
        assert actual.size() == 1
        assertEqualLayouts(actual, tested)
    }

    def "split empty request"() {
        setup:
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                )
        )
        when:
        def actual = tested.split(3)
        then:
        assert actual.size() == 1
        assertEqualLayouts(actual, tested)
    }

    def "split many events"() {
        setup:
        def maxCount = 3
        def overallCount = 33
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                events: createEvents(overallCount)
                        )
                )
        )
        when:
        def actual = tested.split(maxCount)
        then:
        assert actual.size() == 11
        assertMaximumItemCount(actual, maxCount)
        assertUniqueItemCount(actual, overallCount)
        assertEqualLayouts(actual, tested)
    }

    def "split many actions"() {
        setup:
        def maxCount = 3
        def overallCount = 33
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                actions: createActions(overallCount)
                        )
                )
        )
        when:
        def actual = tested.split(maxCount)
        then:
        assert actual.size() == 11
        assertMaximumItemCount(actual, maxCount)
        assertUniqueItemCount(actual, overallCount)
        assertEqualLayouts(actual, tested)
    }

    def "split many conversions"() {
        setup:
        def maxCount = 3
        def overallCount = 33
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                conversions: createConversions(overallCount)
                        )
                )
        )
        when:
        def actual = tested.split(maxCount)
        then:
        assert actual.size() == 11
        assertMaximumItemCount(actual, maxCount)
        assertUniqueItemCount(actual, overallCount)
        assertEqualLayouts(actual, tested)
    }

    def "don't split some actions"() {
        setup:
        def actionCount = 1200  // 1300 would be too much!!!
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                actions: createActions(actionCount)
                        )
                )
        )
        when:
        def actual = tested.split(actionCount)
        then:
        assert actual.size() == 1
        assertMaximumItemCount(actual, actionCount)
        assertUniqueItemCount(actual, actionCount)
        assertEqualLayouts(actual, tested)
        AzureEventHubService.checkObjectSize(actual[0])
    }

    def "don't split few mixed items"() {
        setup:
        def eventCount = 24
        def actionCount = 12
        def conversionCount = 3
        def overallCount = eventCount + actionCount + conversionCount
        def maxCount = 64
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                events: createEvents(eventCount),
                                actions: createActions(actionCount),
                                conversions: createConversions(conversionCount)
                        )
                )
        )
        when:
        def actual = tested.split(maxCount)
        then:
        assert actual.size() == 1
        assertMaximumItemCount(actual, maxCount)
        assertUniqueItemCount(actual, overallCount)
        assertEqualLayouts(actual, tested)
    }

    def "split many mixed items"() {
        setup:
        def eventCount = 240
        def actionCount = 120
        def conversionCount = 32
        def overallCount = eventCount + actionCount + conversionCount
        def maxCount = 64
        def tested = new LayoutCtx(
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                events: createEvents(eventCount),
                                actions: createActions(actionCount),
                                conversions: createConversions(conversionCount)
                        )
                )
        )
        when:
        def actual = tested.split(maxCount)
        then: "it should split into 7 items, each with identical request"
        assert actual.size() == 7
        assertMaximumItemCount(actual, maxCount)
        assertUniqueItemCount(actual, overallCount)
        assertEqualLayouts(actual, tested)
    }

    def "split items should not contain the response or the 'syncApplicationRequest' "() {
        setup:
        def maxCount = 1
        def tested = new LayoutCtx(
                response: new LayoutResponse(
                    accountProximityUUIDs: [ "foo" ],
                    actions: [
                            new LayoutAction( eid: UUID.randomUUID().toString() )
                    ]
                ),
                request: new LayoutRequest(
                        activity: new LayoutRequestBody(
                                events: createEvents(2),
                                actions: createActions(2),
                                conversions: createConversions(2)
                        )
                )
        )
        when:
        def actual = tested.split(maxCount)
        then: "the response and the syncApplicationRequest should not be split"
        actual.each {
            assert it.response == null
            assert it.syncApplicationRequest == null
        }

    }

    def assertMaximumItemCount(List<LayoutCtx> contexts, int maxCount) {
        contexts.eachWithIndex { it, i ->
            int count = 0;
            count += LayoutCtx.listSize(it.request?.activity?.events)
            count += LayoutCtx.listSize(it.request?.activity?.actions)
            count += LayoutCtx.listSize(it.request?.activity?.conversions)
            assert count <= maxCount , "For the list element with index $i count is too big ($count >= $maxCount)"
        }
        true
    }
    def assertUniqueItemCount(List<LayoutCtx> contexts, int overallCount) {
        def itemSet = new IdentityHashMap<Object, Boolean>(overallCount * 2)
        contexts.each {
            it.request?.activity?.events?.each {
                itemSet.put(it, TRUE)
            }
            it.request?.activity?.actions?.each {
                itemSet.put(it, TRUE)
            }
            it.request?.activity?.conversions?.each {
                itemSet.put(it, TRUE)
            }
        }
        assert itemSet.size() == overallCount
        true
    }

    def assertEqualLayouts(List<LayoutCtx> contexts, LayoutCtx base) {
        contexts.each {
            assertEqualLayout(it, base)
        }
        true
    }

    def assertEqualLayout(LayoutCtx ctx, LayoutCtx base) {
        assert ctx.id.startsWith(base.id)
        assert ctx.eventDate == base.eventDate
        assert ctx.elapsedTime == base.elapsedTime
        assert ctx.reportedBack == base.reportedBack
        assert ctx.response == null
        assert ctx.syncApplicationRequest == null

        assertEqualRequest(ctx.request, base.request)
    }

    def assertEqualRequest(LayoutRequest request, LayoutRequest baseRequest) {
        if (baseRequest == null) {
            assert request == null
        } else {
            assert request.diRaw == baseRequest.diRaw
            assert request.deviceId == baseRequest.deviceId
            assert request.advertisingIdentifier == baseRequest.advertisingIdentifier
            assert request.di == baseRequest.di
            assert request.apiKey == baseRequest.apiKey
            assert request.geohash == baseRequest.geohash
            assert request.geohashComputed == baseRequest.geohashComputed
            assert request.pid == baseRequest.pid
            assert request.remoteAddr == baseRequest.remoteAddr
            assert request.qos == baseRequest.qos
            assert request.etag == baseRequest.etag
            assert request.largeApplication == baseRequest.largeApplication
            assert request.invalid == baseRequest.invalid

            if (baseRequest.activity == null) {
                assert request.activity == null
            } else {
                assert request.activity != null
            }
        }
        true
    }

    def createEvents(int count) {
        return createObjects(count, { new LayoutRequestEvent() })
    }
    def createActions(int count) {
        return createObjects(count) {
            // create action with maximum size (in later JSON)
            new LayoutRequestAction(
                    id: newUuid(),
                    eid: newUuid(),
                    pid: newUuid() + "1234512345",
                    dt: new Date(),
                    location: "123456789",
                    trigger: 1
            )
        }
    }
    def createConversions(int count) {
        return createObjects(count, { new LayoutRequestConversion() })
    }
    def createObjects(int count, Closure factory) {
        if (count < 0) {
            return null
        } else if (count == 0) {
            return emptyList()
        } else {
           def result = new ArrayList(count)
            (1..count).each {
                result.add(factory())
            }
            return result
        }
    }

    def newUuid() {
        UUID.randomUUID().toString()
    }
}
