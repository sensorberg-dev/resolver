package com.sensorberg.front.resolve.helpers

import spock.lang.Specification
/**
 * Created by falkorichter on 04.09.15.
 */
public class LayoutLogReportedBackHistoryScriptTest extends Specification {

    def "the layoutLogReportConverter should move reportedBack to reportedBackHistory"(){
        when:
        def input = [ currentStatus : "foo",
                      ctx : [
                              _source : [
                                      reportedBack : "bar"
                              ]
                      ]
                ]

        def values = runScript(input)
        then:
        assert values.ctx._source.reportedBack == "foo"
        assert values.ctx._source.reportedBackHistory == [ "bar", "foo" ]

    }

    def "the layoutLogReportConverter should append to  reportedBackHistory"(){
        when:
        def input = [ currentStatus : "foo",
                      ctx : [
                              _source : [
                                      reportedBack : "bar",
                                      reportedBackHistory : [ "baz", "bar" ]
                              ]
                      ]
        ]
        def values = runScript(input)

        then:
        assert values.ctx._source.reportedBack == "foo"
        assert values.ctx._source.reportedBackHistory == [ "baz", "bar", "foo" ]

    }

    def "should not fail with no reportedBack Status"(){
        when:
        def input = [ currentStatus : "foo",
                      ctx : [
                              _source : [
                                      :
                              ]
                      ]
        ]

        def values = runScript(input)
        then:
        assert values.ctx._source.reportedBack == "foo"
        assert values.ctx._source.reportedBackHistory == [  "foo" ]
    }

    def Map<String, Object> runScript(LinkedHashMap<String, Object> input) {
        return ScriptProvider.runScript(
                ScriptProvider.fileNames.layoutLogReportedBackHistory,
                input,
                input.keySet()
        )
    }
}
