package com.example.ui.game3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    stickRadius: Dp = 32.dp,
    baseColor: Color = Color(0x66000000),
    stickColor: Color = Color(0xDD34D399),
    ringColor: Color = Color(0xAA10B981),
    onMove: (x: Float, y: Float) -> Unit
) {
    var stickOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val maxRadius = (size.toPx() / 2f) - stickRadius.toPx()
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        val delta = offset - center
                        val dist = delta.getDistance()
                        val clampedOffset = if (dist > maxRadius) {
                            delta * (maxRadius / dist)
                        } else delta
                        stickOffset = clampedOffset
                        val normalizedX = (clampedOffset.x / maxRadius).coerceIn(-1f, 1f)
                        val normalizedY = (-clampedOffset.y / maxRadius).coerceIn(-1f, 1f)
                        onMove(normalizedX, normalizedY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = stickOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clampedOffset = if (dist > maxRadius) {
                            newOffset * (maxRadius / dist)
                        } else newOffset
                        stickOffset = clampedOffset
                        val normalizedX = (clampedOffset.x / maxRadius).coerceIn(-1f, 1f)
                        val normalizedY = (-clampedOffset.y / maxRadius).coerceIn(-1f, 1f)
                        onMove(normalizedX, normalizedY)
                    },
                    onDragEnd = {
                        stickOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        stickOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = this.size.width / 2f

            // Outer Base Disk
            drawCircle(
                color = baseColor,
                radius = outerRadius,
                center = center
            )
            // Accent Ring
            drawCircle(
                color = ringColor,
                radius = outerRadius - 4f,
                center = center,
                style = Stroke(width = 3f)
            )
            // Inner Stick Thumb
            drawCircle(
                color = stickColor,
                radius = stickRadius.toPx(),
                center = center + stickOffset
            )
            // Inner Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = stickRadius.toPx() * 0.45f,
                center = center + stickOffset - Offset(stickRadius.toPx() * 0.2f, stickRadius.toPx() * 0.2f)
            )
        }
    }
}
