package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.DeviceIdentifier
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * user agent parser
 */
class UserAgentParser {
    private static final Pattern iosDevicePattern = Pattern.compile("([^/]*)/([^/]*)/([^\\s]*) \\(iOS ([^\\)]*)\\) \\(([^\\s]*) ([^\\)]*)\\) Sensorberg SDK (.*)")
    private static final Pattern androidDevicePattern = Pattern.compile("([^/]*)/([^/]*)/([^\\s]*) \\(Android ([^\\s]*) ([^\\)]*)\\) \\(([^:]*):([^:)]*):([^\\))]*)\\) Sensorberg SDK (.*)")

    public static DeviceIdentifier toDeviceIdentifier(String userAgentString, String deviceId) {
        if(StringUtils.isEmpty(userAgentString)) {
            return new DeviceIdentifier()
        }
        Matcher matcher
        if((matcher = iosDevicePattern.matcher(userAgentString)).matches()) {
            return new DeviceIdentifier(
                    applicationLabel: matcher.group(1),
                    packageName: matcher.group(2),
                    applicationVersion: matcher.group(3),
                    os: "iOS",
                    osVersion: matcher.group(4),
                    cpu: "Ax",
                    deviceManufacturer: "Apple",
                    deviceModel: matcher.group(5),
                    deviceProduct: matcher.group(6),
                    sdkVersion: matcher.group(7),
                    deviceId: deviceId
            )
        } else if((matcher = androidDevicePattern.matcher(userAgentString)).matches()) {
            return new DeviceIdentifier(
                    applicationLabel: matcher.group(1),
                    packageName: matcher.group(2),
                    applicationVersion: matcher.group(3),
                    os: "Android",
                    osVersion: matcher.group(4),
                    cpu: matcher.group(5),
                    deviceManufacturer: matcher.group(6),
                    deviceModel: matcher.group(7),
                    deviceProduct: matcher.group(8),
                    sdkVersion: matcher.group(9),
                    deviceId: deviceId
            )
        }
        return new DeviceIdentifier();
    }
}
