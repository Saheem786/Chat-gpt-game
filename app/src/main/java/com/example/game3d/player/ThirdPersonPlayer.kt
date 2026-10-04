package com.example.game3d.player

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.game3d.collision.WorldCollisionSystem
import com.example.game3d.world.mapper.FarmWorldPositionMapper

class ThirdPersonPlayer(
    startX: Float = 0f,
    startY: Float = 0f,
    startZ: Float = -6f,
    startYaw: Float = 180f
) {
    val position = Vector3(startX, startY, startZ)
    val velocity = Vector3(0f, 0f, 0f)
    var yaw: Float = startYaw
    var targetYaw: Float = startYaw

    var isMoving: Boolean = false
    var isRunning: Boolean = false
    var walkTimer: Float = 0f

    private val playerRadius = 0.45f

    init {
        // Snap to terrain on init
        position.y = FarmWorldPositionMapper.getTerrainHeight(position.x, position.z)
    }

    fun update(delta: Float, input: PlayerInputState, cameraYaw: Float) {
        val inputX = input.moveX
        val inputY = input.moveY
        isRunning = input.isRunning

        val hasInput = Math.abs(inputX) > 0.05f || Math.abs(inputY) > 0.05f

        if (hasInput) {
            isMoving = true
            val speed = if (isRunning) 8.5f else 4.5f

            // Calculate world move direction relative to camera yaw
            val camRad = cameraYaw * MathUtils.degreesToRadians
            val cosCam = MathUtils.cos(camRad)
            val sinCam = MathUtils.sin(camRad)

            // Forward is along camera view, strafe is perpendicular
            val worldMoveX = inputX * cosCam + inputY * sinCam
            val worldMoveZ = -inputX * sinCam + inputY * cosCam

            val moveLength = Math.sqrt((worldMoveX * worldMoveX + worldMoveZ * worldMoveZ).toDouble()).toFloat()
            if (moveLength > 0.001f) {
                val dirX = worldMoveX / moveLength
                val dirZ = worldMoveZ / moveLength

                targetYaw = MathUtils.atan2(dirX, dirZ) * MathUtils.radiansToDegrees

                val targetVelX = dirX * speed
                val targetVelZ = dirZ * speed

                velocity.x = MathUtils.lerp(velocity.x, targetVelX, delta * 14f)
                velocity.z = MathUtils.lerp(velocity.z, targetVelZ, delta * 14f)
            }

            // Update walk cycle timer
            val animSpeed = if (isRunning) 14f else 8f
            walkTimer += delta * animSpeed
        } else {
            isMoving = false
            velocity.x = MathUtils.lerp(velocity.x, 0f, delta * 12f)
            velocity.z = MathUtils.lerp(velocity.z, 0f, delta * 12f)
            walkTimer += delta * 2.5f
        }

        // Smooth yaw rotation
        var diff = (targetYaw - yaw) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        yaw += diff * MathUtils.clamp(delta * 14f, 0f, 1f)

        // Resolve movement and wall-sliding collisions
        val desiredX = position.x + velocity.x * delta
        val desiredZ = position.z + velocity.z * delta

        val (resolvedX, resolvedZ) = WorldCollisionSystem.resolvePlayerMovement(
            currentX = position.x,
            currentZ = position.z,
            targetX = desiredX,
            targetZ = desiredZ,
            radius = playerRadius
        )

        position.x = resolvedX
        position.z = resolvedZ

        // Follow terrain height dynamically
        val groundY = FarmWorldPositionMapper.getTerrainHeight(position.x, position.z)
        position.y = MathUtils.lerp(position.y, groundY, delta * 20f)
    }
}
