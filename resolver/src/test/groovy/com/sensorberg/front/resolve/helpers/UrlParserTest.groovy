package com.sensorberg.front.resolve.helpers

import spock.lang.Specification

class UrlParserTest extends Specification {
    def "Parse"() {
        expect:
        with(UrlParser.parse(urlString)) {
            assert protocol == protocolString
            assert host == hostString
            assert path == pathString
            assert queryParams == queryParamsMap
        }
        where:
        urlString | protocolString | hostString | pathString | queryParamsMap
        "http://sensorberg-dev.github.io/resolver?a=1&b=2" | "http" | "sensorberg-dev.github.io" | "/resolver" | ["a": "1", "b": "2"]
        "https://abc.com:9090/t1/t2/t3?a=b" | "https" | "abc.com" | "/t1/t2/t3" | ["a": "b"]
    }

    def "EndpointString"() {
        expect:
        def parsed = UrlParser.parse(urlString)
        assert parsed.getEndpoint() == endpointString
        assert parsed.port == portInt
        where:
        urlString | endpointString | portInt
        "http://sensorberg-dev.github.io/resolver?a=1&b=2" | "http://sensorberg-dev.github.io/resolver" | 80
        "https://sensorberg-dev.github.io/resolver?a=1&b=2" | "https://sensorberg-dev.github.io/resolver" | 443
        "http://sensorberg-dev.github.io:80/resolver?a=1&b=2" | "http://sensorberg-dev.github.io/resolver" | 80
        "https://sensorberg-dev.github.io:443/resolver?a=1&b=2" | "https://sensorberg-dev.github.io/resolver" | 443
        "http://sensorberg-dev.github.io:811/resolver?a=1&b=2" | "http://sensorberg-dev.github.io:811/resolver" | 811
        "http://sensorberg-dev.github.io:443/resolver?a=1&b=2" | "http://sensorberg-dev.github.io:443/resolver" | 443
        "https://sensorberg-dev.github.io:80/resolver?a=1&b=2" | "https://sensorberg-dev.github.io:80/resolver" | 80
    }
}
