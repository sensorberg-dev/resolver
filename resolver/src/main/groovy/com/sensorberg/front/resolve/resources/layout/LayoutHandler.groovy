package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx

/**
 * layout handler interface
 */
interface LayoutHandler {
    LayoutCtx getForBeacon(LayoutCtx ctx)
    LayoutCtx get(LayoutCtx ctx)
}