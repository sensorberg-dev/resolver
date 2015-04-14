package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx

/**
 * layout handler interface
 */
interface LayoutHandler {
    def getForBeacon(LayoutCtx ctx)
    def get(LayoutCtx ctx)
}