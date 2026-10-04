package com.example.game3d.player

import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.game3d.collision.WorldCollisionSystem

class ThirdPersonCamera(
    val camera: PerspectiveCamera,
    initialYaw: Float = 180f,
    initialPitch: Float = 22f,
    initialDistance: Float = 5.2f
) {
    var desiredDistance: Float = initialDistance
    var currentDistance: Float = initialDistance
    var yaw: Float = initialYaw
    var pitch: Float = initialPitch

    private val minDistance = 1.8f
    private val maxDistance = 9.5f
    private val minPitch = -10f
    private val maxPitch = 65f

    private val focusPoint = Vector3()
    private val desiredCamPos = Vector3()
    private val resolvedCamPos = Vector3()

    fun update(delta: Float, player: ThirdPersonPlayer, input: PlayerInputState) {
        // Apply input rotations
        yaw -= input.lookDeltaX * 0.18f
        pitch = MathUtils.clamp(pitch - input.lookDeltaY * 0.18f, minPitch, maxPitch)

        // Apply zoom
        desiredDistance = MathUtils.clamp(desiredDistance - input.zoomDelta * 0.05f, minDistance, maxDistance)

        // Focus point is slightly above player center
        focusPoint.set(player.position.x, player.position.y + 1.45f, player.position.z)

        // Convert spherical coords to Cartesian offset for desired camera position
        val yawRad = yaw * MathUtils.degreesToRadians
        val pitchRad = pitch * MathUtils.degreesToRadians

        val cosPitch = MathUtils.cos(pitchRad)
        val sinPitch = MathUtils.sin(pitchRad)
        val cosYaw = MathUtils.cos(yawRad)
        val sinYaw = MathUtils.sin(yawRad)

        val offsetX = desiredDistance * cosPitch * sinYaw
        val offsetY = desiredDistance * sinPitch
        val offsetZ = desiredDistance * cosPitch * cosYaw

        desiredCamPos.set(
            focusPoint.x + offsetX,
            focusPoint.y + offsetY,
            focusPoint.z + offsetZ
        )

        // Check and resolve camera obstruction against world buildings, props, trees, and terrain
        val unobstructedPos = WorldCollisionSystem.resolveCameraObstruction(
            focusPoint = focusPoint,
            desiredCamPos = desiredCamPos,
            minDistance = minDistance
        )

        // Smoothly interpolate camera position
        camera.position.lerp(unobstructedPos, MathUtils.clamp(delta * 18f, 0f, 1f))
        camera.lookAt(focusPoint.x, focusPoint.y, focusPoint.z)
        camera.up.set(Vector3.Y)
        camera.update()
    }
}
