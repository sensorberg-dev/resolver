package com.sensorberg.front.resolve.resources.layout.domain

/**
 * action types
 */
trait ActionType {

    private static final def typeMap = [
            1: "notification",
            2: "website",
            3: "function",
            4: "coupon",
            5: "vibrate",
            9: "custom",
            11: "image",
            12: "video",
            13: "audio",
            31: "mailing"
    ]

    Integer type

    String typeString() {
        return typeMap.get(type) ?: type
    }

}