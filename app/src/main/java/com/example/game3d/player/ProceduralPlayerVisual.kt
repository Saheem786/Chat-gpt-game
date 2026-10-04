package com.example.game3d.player

import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.game3d.renderer.ModelFactory

class ProceduralPlayerVisual(
    modelFactory: ModelFactory
) : IPlayerVisual {

    private val torsoInstance = ModelInstance(modelFactory.createPlayerBody())
    private val headInstance = ModelInstance(modelFactory.createPlayerHead())
    private val leftArmInstance = ModelInstance(modelFactory.createPlayerLimb(true))
    private val rightArmInstance = ModelInstance(modelFactory.createPlayerLimb(true))
    private val leftLegInstance = ModelInstance(modelFactory.createPlayerLimb(false))
    private val rightLegInstance = ModelInstance(modelFactory.createPlayerLimb(false))

    var leftArmAngle: Float = 0f
    var rightArmAngle: Float = 0f
    var leftLegAngle: Float = 0f
    var rightLegAngle: Float = 0f
    var bodyBobY: Float = 0f

    override fun updateAnimation(delta: Float, isMoving: Boolean, isRunning: Boolean, walkTimer: Float) {
        if (isMoving) {
            val maxLimbAngle = if (isRunning) 45f else 28f
            leftArmAngle = MathUtils.sin(walkTimer) * maxLimbAngle
            rightArmAngle = -leftArmAngle
            leftLegAngle = -MathUtils.sin(walkTimer) * maxLimbAngle
            rightLegAngle = -leftLegAngle
            bodyBobY = Math.abs(MathUtils.sin(walkTimer * 2f)) * (if (isRunning) 0.08f else 0.04f)
        } else {
            leftArmAngle = MathUtils.lerp(leftArmAngle, 0f, delta * 8f)
            rightArmAngle = MathUtils.lerp(rightArmAngle, 0f, delta * 8f)
            leftLegAngle = MathUtils.lerp(leftLegAngle, 0f, delta * 8f)
            rightLegAngle = MathUtils.lerp(rightLegAngle, 0f, delta * 8f)
            bodyBobY = MathUtils.sin(walkTimer) * 0.02f
        }
    }

    override fun render(modelBatch: ModelBatch, environment: Environment, position: Vector3, yaw: Float) {
        val px = position.x
        val py = position.y + bodyBobY
        val pz = position.z

        // Torso
        torsoInstance.transform.setToTranslation(px, py + 1.0f, pz)
        torsoInstance.transform.rotate(Vector3.Y, yaw)

        // Head
        headInstance.transform.setToTranslation(px, py + 1.55f, pz)
        headInstance.transform.rotate(Vector3.Y, yaw)

        // Left Arm
        leftArmInstance.transform.setToTranslation(px, py + 1.05f, pz)
        leftArmInstance.transform.rotate(Vector3.Y, yaw)
        leftArmInstance.transform.translate(-0.35f, 0f, 0f)
        leftArmInstance.transform.rotate(Vector3.X, leftArmAngle)

        // Right Arm
        rightArmInstance.transform.setToTranslation(px, py + 1.05f, pz)
        rightArmInstance.transform.rotate(Vector3.Y, yaw)
        rightArmInstance.transform.translate(0.35f, 0f, 0f)
        rightArmInstance.transform.rotate(Vector3.X, rightArmAngle)

        // Left Leg
        leftLegInstance.transform.setToTranslation(px, py + 0.45f, pz)
        leftLegInstance.transform.rotate(Vector3.Y, yaw)
        leftLegInstance.transform.translate(-0.16f, 0f, 0f)
        leftLegInstance.transform.rotate(Vector3.X, leftLegAngle)

        // Right Leg
        rightLegInstance.transform.setToTranslation(px, py + 0.45f, pz)
        rightLegInstance.transform.rotate(Vector3.Y, yaw)
        rightLegInstance.transform.translate(0.16f, 0f, 0f)
        rightLegInstance.transform.rotate(Vector3.X, rightLegAngle)

        // Draw in batch
        modelBatch.render(torsoInstance, environment)
        modelBatch.render(headInstance, environment)
        modelBatch.render(leftArmInstance, environment)
        modelBatch.render(rightArmInstance, environment)
        modelBatch.render(leftLegInstance, environment)
        modelBatch.render(rightLegInstance, environment)
    }

    override fun dispose() {
        // Models are managed by ModelFactory
    }
}
