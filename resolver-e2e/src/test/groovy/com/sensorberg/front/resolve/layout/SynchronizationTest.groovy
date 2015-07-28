package com.sensorberg.front.resolve.layout

import com.sensorberg.front.resolve.helpers.IndexHelper
import com.sensorberg.front.resolve.helpers.ResolverLayoutHelper
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by Andreas Dörner on 27.07.15.
 */
class SynchronizationTest extends Specification {

    @Shared IndexHelper indexHelper = new IndexHelper()

    def "delete full index"() {
        when:
        def response = IndexHelper.deleteIndex()
        then:
        assert response == "[success: true]"
    }

}
