package com.example.game3d.world

import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.data.local.AnimalEntity
import com.example.data.model.AnimalSpecies
import kotlin.random.Random

enum class AnimalAIState {
    IDLE,
    WANDER,
    GRAZE,
    REST
}

class Animal3DEntity(
    val entityId: Long,
    val species: AnimalSpecies,
    var nickname: String,
    initialX: Float,
    initialZ: Float,
    val instance: ModelInstance
) {
    val position = Vector3(initialX, 0.1f, initialZ)
    var yaw: Float = Random.nextFloat() * 360f
    var targetYaw: Float = yaw

    var aiState: AnimalAIState = AnimalAIState.IDLE
    private var stateTimer: Float = Random.nextFloat() * 4f + 2f
    private val targetPos = Vector3(initialX, 0.1f, initialZ)

    // Boundaries based on species housing
    private val minX: Float
    private val maxX: Float
    private val minZ: Float
    private val maxZ: Float

    init {
        when (species) {
            AnimalSpecies.CHICKEN, AnimalSpecies.DUCK -> {
                minX = -21f; maxX = -14f; minZ = 6f; maxZ = 16f
            }
            AnimalSpecies.BEES -> {
                minX = 12f; maxX = 18f; minZ = 5f; maxZ = 12f
            }
            else -> {
                // Cow, Goat, Sheep, Pig in Barn Paddock
                minX = -22f; maxX = -7f; minZ = -6f; maxZ = 8f
            }
        }
    }

    fun syncData(entity: AnimalEntity) {
        nickname = entity.nickname
    }

    fun update(delta: Float, playerPos: Vector3) {
        stateTimer -= delta
        if (stateTimer <= 0f) {
            pickNextState()
        }

        if (aiState == AnimalAIState.WANDER) {
            val toTargetX = targetPos.x - position.x
            val toTargetZ = targetPos.z - position.z
            val dist = Math.sqrt((toTargetX * toTargetX + toTargetZ * toTargetZ).toDouble()).toFloat()

            if (dist > 0.2f) {
                targetYaw = MathUtils.atan2(toTargetX, toTargetZ) * MathUtils.radiansToDegrees
                val speed = if (species == AnimalSpecies.CHICKEN || species == AnimalSpecies.DUCK) 1.2f else 1.0f

                position.x += (toTargetX / dist) * speed * delta
                position.z += (toTargetZ / dist) * speed * delta
            } else {
                aiState = AnimalAIState.GRAZE
                stateTimer = Random.nextFloat() * 4f + 3f
            }
        }

        // Smooth yaw rotation
        var diff = (targetYaw - yaw) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        yaw += diff * MathUtils.clamp(delta * 6f, 0f, 1f)

        // Update 3D ModelInstance transform
        instance.transform.setToTranslation(position.x, position.y, position.z)
        instance.transform.rotate(Vector3.Y, yaw)

        // Subtle bobbing when grazing / wandering
        if (aiState == AnimalAIState.GRAZE) {
            val grazeBob = Math.sin((System.currentTimeMillis() % 1000) / 1000.0 * Math.PI * 2.0).toFloat() * 0.03f
            instance.transform.trn(0f, grazeBob, 0f)
        }
    }

    private fun pickNextState() {
        val r = Random.nextFloat()
        if (species == AnimalSpecies.BEES) {
            aiState = AnimalAIState.IDLE
            stateTimer = 5f
            return
        }

        when {
            r < 0.45f -> {
                aiState = AnimalAIState.WANDER
                targetPos.set(
                    Random.nextFloat() * (maxX - minX) + minX,
                    0.1f,
                    Random.nextFloat() * (maxZ - minZ) + minZ
                )
                stateTimer = Random.nextFloat() * 5f + 3f
            }
            r < 0.75f -> {
                aiState = AnimalAIState.GRAZE
                stateTimer = Random.nextFloat() * 6f + 3f
            }
            r < 0.90f -> {
                aiState = AnimalAIState.IDLE
                stateTimer = Random.nextFloat() * 4f + 2f
            }
            else -> {
                aiState = AnimalAIState.REST
                stateTimer = Random.nextFloat() * 7f + 4f
            }
        }
    }
}
