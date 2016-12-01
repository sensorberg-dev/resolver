package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx

/**
 * layout handler interface
 */
interface LayoutHandler {
    LayoutCtx get(LayoutCtx ctx)
}