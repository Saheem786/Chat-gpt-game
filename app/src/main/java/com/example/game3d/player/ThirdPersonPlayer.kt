package com.example.game3d.player

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox

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

    // Procedural animation limb angles
    var leftArmAngle: Float = 0f
    var rightArmAngle: Float = 0f
    var leftLegAngle: Float = 0f
    var rightLegAngle: Float = 0f
    var bodyBobY: Float = 0f

    private val playerRadius = 0.5f

    // Static obstacle bounding boxes in world coordinates
    private val obstacles = listOf(
        // Farm House
        BoundingBox(Vector3(-4.5f, 0f, -17f), Vector3(4.5f, 5f, -9f)),
        // Workshop
        BoundingBox(Vector3(-16f, 0f, -22f), Vector3(-8f, 5f, -14f)),
        // Eco Shop
        BoundingBox(Vector3(8f, 0f, -22f), Vector3(16f, 5f, -14f)),
        // Barn
        BoundingBox(Vector3(-24f, 0f, -5f), Vector3(-14f, 6f, 7f)),
        // Chicken Coop
        BoundingBox(Vector3(-22f, 0f, 9f), Vector3(-16f, 4f, 15f)),
        // Greenhouse
        BoundingBox(Vector3(10f, 0f, 12f), Vector3(18f, 4f, 20f)),
        // Water Tank / Rain Tower
        BoundingBox(Vector3(-3f, 0f, 18f), Vector3(3f, 6f, 24f)),
        // Duck Pond Water Hazard
        BoundingBox(Vector3(-18f, -1f, 16f), Vector3(-10f, 0.5f, 24f))
    )

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

                velocity.x = MathUtils.lerp(velocity.x, targetVelX, delta * 12f)
                velocity.z = MathUtils.lerp(velocity.z, targetVelZ, delta * 12f)
            }

            // Procedural animation update
            val animSpeed = if (isRunning) 14f else 8f
            walkTimer += delta * animSpeed

            val maxLimbAngle = if (isRunning) 45f else 28f
            leftArmAngle = MathUtils.sin(walkTimer) * maxLimbAngle
            rightArmAngle = -leftArmAngle
            leftLegAngle = -MathUtils.sin(walkTimer) * maxLimbAngle
            rightLegAngle = -leftLegAngle
            bodyBobY = Math.abs(MathUtils.sin(walkTimer * 2f)) * (if (isRunning) 0.08f else 0.04f)
        } else {
            isMoving = false
            velocity.x = MathUtils.lerp(velocity.x, 0f, delta * 10f)
            velocity.z = MathUtils.lerp(velocity.z, 0f, delta * 10f)

            // Idle breathing animation
            walkTimer += delta * 2.5f
            leftArmAngle = MathUtils.lerp(leftArmAngle, 0f, delta * 8f)
            rightArmAngle = MathUtils.lerp(rightArmAngle, 0f, delta * 8f)
            leftLegAngle = MathUtils.lerp(leftLegAngle, 0f, delta * 8f)
            rightLegAngle = MathUtils.lerp(rightLegAngle, 0f, delta * 8f)
            bodyBobY = MathUtils.sin(walkTimer) * 0.02f
        }

        // Smooth yaw rotation
        var diff = (targetYaw - yaw) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        yaw += diff * MathUtils.clamp(delta * 14f, 0f, 1f)

        // Attempt move with collision checks
        val newX = position.x + velocity.x * delta
        val newZ = position.z + velocity.z * delta

        // Farm world boundary limits
        val clampedX = MathUtils.clamp(newX, -34f, 34f)
        val clampedZ = MathUtils.clamp(newZ, -34f, 34f)

        // Check obstacle collisions
        var canMoveX = true
        var canMoveZ = true
        val tempVec = Vector3()

        for (obs in obstacles) {
            if (obs.contains(tempVec.set(clampedX, position.y + 0.5f, position.z))) {
                canMoveX = false
            }
            if (obs.contains(tempVec.set(position.x, position.y + 0.5f, clampedZ))) {
                canMoveZ = false
            }
        }

        if (canMoveX) position.x = clampedX
        if (canMoveZ) position.z = clampedZ
    }
}
