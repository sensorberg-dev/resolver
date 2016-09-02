package com.sensorberg.front.resolve.resources.layout.domain

/**
 * action types
 */
trait ActionType {

    private static final def typeMap = [
            1: "notification",
            2: "website",
            3: "function",
            4: "silent"
    ]

    Integer type

    String typeString() {
        return typeMap.get(type) ?: type
    }

}