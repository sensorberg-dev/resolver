package com.sensorberg.front.resolve.resources.layout.domain

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.AutoClone

/**
 * device identifier parsed from UserAgent header by UserAgentParser
 */
@AutoClone
@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
class DeviceIdentifier {
    String applicationLabel
    String packageName
    String applicationVersion
    String os
    String osVersion
    String cpu
    String deviceManufacturer
    String deviceModel
    String deviceProduct
    String sdkVersion
    String deviceId
}
