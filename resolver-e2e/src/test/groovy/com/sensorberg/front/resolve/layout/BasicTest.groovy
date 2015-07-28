package com.sensorberg.front.resolve.layout

import com.sensorberg.front.resolve.helpers.ResolverLayoutHelper
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestAction
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestBody
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification



class BasicTest extends Specification {

    @Shared ResolverLayoutHelper resolver = new ResolverLayoutHelper()

    final def apiKey = "a9677402e38469a9db58dd51266e912909d769d5e272b28968b7aca3c991cc46"


    // TODO: wir müssen erst die ES mit Daten füllen.
    def "basic test"() {
        when:
        def response = resolver.layout(apiKey)
        then:
        assert response != null
    }

    def "post test"() {
        given:
        def action = resolver.layout(apiKey).actions[0]
        when:
        def response = resolver.layout(
                apiKey,
                new LayoutRequestBody(
                        deviceTimestamp: new Date(),
                        actions: [
                                new LayoutRequestAction(
                                        eid: action.eid,
                                        pid: action.beacons[0],
                                        dt: new Date(),
                                        location: "here",
                                        trigger: 1
                                )
                        ]
                )
        )
        then:
        assert response != null
    }

    def "nonexisting apiKey test"() {
        when:
        def layout = resolver.layout("non existing api key")
        then:
        assert layout == null, "for unknown api key do not send empty layout because it is confusing"
    }

}
