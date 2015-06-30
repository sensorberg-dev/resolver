package com.sensorberg.front.resolve.helpers

class ParsedUrl {
    String protocol
    String host
    int port
    String path
    Map<String, String> queryParams

    /**
     * get endpoint part for the url (w/o queryString)
     * example url: https://test.com/any/path?any=queryString will return https://test.com/any/path
     * this one will try to skip port it one is default for given protocol
     * @return endpoint string
     */
    String getEndpoint() {
        String portString = (
                    (protocol?.equalsIgnoreCase("http") && port == 80) ||
                    (protocol?.equalsIgnoreCase("https") && port == 443) ||
                    port == 0 || port == -1
        ) ? "" : ":$port"
        return "$protocol://$host$portString$path"
    }
}
