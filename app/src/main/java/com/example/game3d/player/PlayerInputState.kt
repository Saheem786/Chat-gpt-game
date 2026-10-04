package com.example.game3d.player

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class PlayerInputState {
    @Volatile var moveX: Float = 0f
    @Volatile var moveY: Float = 0f
    @Volatile var isRunning: Boolean = false
    @Volatile var lookDeltaX: Float = 0f
    @Volatile var lookDeltaY: Float = 0f
    @Volatile var zoomDelta: Float = 0f

    val actionTriggered = AtomicBoolean(false)

    fun resetDeltas() {
        lookDeltaX = 0f
        lookDeltaY = 0f
        zoomDelta = 0f
    }

    fun setMovement(x: Float, y: Float) {
        val len = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        if (len > 1.0f) {
            moveX = x / len
            moveY = y / len
        } else {
            moveX = x
            moveY = y
        }
    }

    fun addLookDelta(dx: Float, dy: Float) {
        lookDeltaX += dx
        lookDeltaY += dy
    }

    fun addZoom(delta: Float) {
        zoomDelta += delta
    }
}
