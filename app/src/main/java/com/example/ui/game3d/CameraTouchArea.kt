package com.example.ui.game3d

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun CameraTouchArea(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRotate: (dx: Float, dy: Float) -> Unit,
    onZoom: (delta: Float) -> Unit
) {
    Box(
        modifier = modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput

            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val width = size.width.toFloat()
                val height = size.height.toFloat()

                // Exclude joystick quadrant (bottom left) and bottom right action button quadrant
                val isJoystickZone = down.position.x < width * 0.45f && down.position.y > height * 0.55f
                val isActionButtonZone = down.position.x > width * 0.72f && down.position.y > height * 0.65f
                val isTopHudZone = down.position.y < height * 0.12f

                if (isJoystickZone || isActionButtonZone || isTopHudZone) {
                    // Let the child controls handle this touch without moving camera
                    return@awaitEachGesture
                }

                do {
                    val event = awaitPointerEvent()
                    val canceled = event.changes.any { it.isConsumed }
                    if (!canceled) {
                        val pointerCount = event.changes.size

                        if (pointerCount == 1) {
                            val change = event.changes.first()
                            val pan = change.position - change.previousPosition
                            if (pan.x != 0f || pan.y != 0f) {
                                onRotate(pan.x, pan.y)
                                change.consume()
                            }
                        } else if (pointerCount >= 2) {
                            val zoom = event.calculateZoom()
                            if (zoom != 1f) {
                                val zoomDelta = (zoom - 1f) * 15f
                                onZoom(zoomDelta)
                            }
                            val pan = event.calculatePan()
                            if (pan.x != 0f || pan.y != 0f) {
                                onRotate(pan.x * 0.5f, pan.y * 0.5f)
                            }
                            event.changes.forEach { it.consume() }
                        }
                    }
                } while (event.changes.any { it.pressed })
            }
        }
    )
}
