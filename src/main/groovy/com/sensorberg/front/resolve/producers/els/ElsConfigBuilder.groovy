package com.sensorberg.front.resolve.producers.els

import com.sensorberg.front.resolve.producers.els.domain.ElsConfig
import com.sensorberg.front.resolve.producers.els.domain.ElsNode
import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.transport.TransportAddress

/**
 * elastic search configuration builder
 */
@Slf4j
class ElsConfigBuilder {

    private static final DEFAULT_PORT = 9300

    static def numberOrNull(String string) {
        return (string?.isInteger()) ? string.toInteger() : null
    }

    static def parseHostString(String hostString) {
        def parsedHostPort = hostString.split(":")
        def host = parsedHostPort[0]
        def port = (parsedHostPort.length == 2) ?
                numberOrNull(parsedHostPort[1]) ?: DEFAULT_PORT :
                DEFAULT_PORT
        return new ElsNode(host: host, port: port)
    }

    public static ElsConfig build(String connectionString) {
        try {
            def connectionURI = new URI(connectionString)
            return new ElsConfig(
                    clusterName: connectionURI.path.replaceFirst("/", ""),
                    nodes: connectionURI.authority.split(",").collect { String hostPortString ->
                        parseHostString(hostPortString)
                    }
            )
        } catch (NullPointerException | URISyntaxException e) {
            log.error("invalid elasticsearch.connectionString format - this must be valid URI [current value: '{}']", connectionString)
            return null;
        }
    }

    public static Client buildClient(String connectionString) {
        def config = build(connectionString)
        if (!config?.valid) {
            return null
        }
        def settings = ImmutableSettings.settingsBuilder()
        if (!config?.clusterName?.isEmpty()) {
            settings.put("cluster.name", config.clusterName).build()
        }
        TransportAddress[] addresses =
                config.nodes.collect { new InetSocketTransportAddress(it.host, it.port) }.toArray()
        new TransportClient(settings).addTransportAddresses(addresses)
    }
}
