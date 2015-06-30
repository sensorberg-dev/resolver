package com.sensorberg.front.resolve.helpers

import org.apache.http.client.utils.URLEncodedUtils

class UrlParser {

    static def schemeToPortMap = [
            "http": 80,
            "https": 443
    ]

    static parse(String urlString) {
        URI uri = new URI(urlString)
        return new ParsedUrl(
                protocol: uri.scheme,
                host: uri.host,
                path: uri.path,
                port: (uri.port == -1) ? schemeToPortMap.getOrDefault(uri.scheme, uri.port) : uri.port,
                queryParams: URLEncodedUtils.parse(uri, "UTF-8").collectEntries {
                    [it.name, it.value]
                }
        )
    }

}
