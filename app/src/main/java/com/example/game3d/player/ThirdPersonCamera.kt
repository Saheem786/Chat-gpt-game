package com.example.game3d.player

import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3

class ThirdPersonCamera(
    val camera: PerspectiveCamera
) {
    var distance: Float = 5.2f
    var yaw: Float = 180f
    var pitch: Float = 22f

    private val minDistance = 2.5f
    private val maxDistance = 9.5f
    private val minPitch = -10f
    private val maxPitch = 65f

    private val targetPos = Vector3()
    private val currentPos = Vector3()

    fun update(delta: Float, player: ThirdPersonPlayer, input: PlayerInputState) {
        // Apply input rotations
        yaw -= input.lookDeltaX * 0.18f
        pitch = MathUtils.clamp(pitch - input.lookDeltaY * 0.18f, minPitch, maxPitch)

        // Apply zoom
        distance = MathUtils.clamp(distance - input.zoomDelta * 0.05f, minDistance, maxDistance)

        // Target look-at point is slightly above player's center
        val lookAtX = player.position.x
        val lookAtY = player.position.y + 1.4f
        val lookAtZ = player.position.z

        // Convert spherical coords (distance, yaw, pitch) to Cartesian offset
        val yawRad = yaw * MathUtils.degreesToRadians
        val pitchRad = pitch * MathUtils.degreesToRadians

        val cosPitch = MathUtils.cos(pitchRad)
        val sinPitch = MathUtils.sin(pitchRad)
        val cosYaw = MathUtils.cos(yawRad)
        val sinYaw = MathUtils.sin(yawRad)

        val offsetX = distance * cosPitch * sinYaw
        val offsetY = distance * sinPitch
        val offsetZ = distance * cosPitch * cosYaw

        targetPos.set(
            lookAtX + offsetX,
            Math.max(lookAtY + offsetY, 0.4f), // Prevent camera going below ground
            lookAtZ + offsetZ
        )

        // Smooth camera follow
        camera.position.lerp(targetPos, MathUtils.clamp(delta * 16f, 0f, 1f))
        camera.lookAt(lookAtX, lookAtY, lookAtZ)
        camera.up.set(Vector3.Y)
        camera.update()
    }
}
