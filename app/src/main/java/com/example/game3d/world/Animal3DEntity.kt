package com.example.game3d.world

import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.data.local.AnimalEntity
import com.example.data.model.AnimalSpecies
import com.example.game3d.world.mapper.FarmWorldPositionMapper
import kotlin.random.Random

enum class AnimalAIState {
    IDLE,
    WANDER,
    EAT,
    DRINK,
    REST,
    INTERACT
}

class Animal3DEntity(
    val entityId: Long,
    val species: AnimalSpecies,
    var nickname: String,
    initialX: Float,
    initialY: Float,
    initialZ: Float,
    initialYaw: Float,
    val instance: ModelInstance? = null
) {
    val position = Vector3(initialX, initialY, initialZ)
    var yaw: Float = initialYaw
    var targetYaw: Float = initialYaw

    var aiState: AnimalAIState = AnimalAIState.IDLE
    private var stateTimer: Float = Random.nextFloat() * 4f + 2f
    private val targetPos = Vector3(initialX, initialY, initialZ)

    // Species paddock boundary limits
    private val minX: Float
    private val maxX: Float
    private val minZ: Float
    private val maxZ: Float

    init {
        when (species) {
            AnimalSpecies.CHICKEN, AnimalSpecies.DUCK -> {
                minX = -21.0f; maxX = -14.0f; minZ = 6.5f; maxZ = 16.0f
            }
            AnimalSpecies.BEES -> {
                minX = 12.0f; maxX = 18.0f; minZ = 5.0f; maxZ = 12.0f
            }
            else -> {
                // Cow, Goat, Sheep, Pig in Barn Paddock
                minX = -22.0f; maxX = -7.0f; minZ = -5.5f; maxZ = 7.5f
            }
        }
        // Ensure initial position is on terrain
        position.y = FarmWorldPositionMapper.getTerrainHeight(position.x, position.z) + 0.1f
    }

    fun syncData(entity: AnimalEntity) {
        nickname = entity.nickname
    }

    fun update(delta: Float, playerPos: Vector3) {
        stateTimer -= delta
        if (stateTimer <= 0f && aiState != AnimalAIState.INTERACT) {
            pickNextState()
        }

        // Check if player is very close to interact
        val distToPlayer = position.dst(playerPos)
        if (distToPlayer < 2.0f && aiState != AnimalAIState.INTERACT && species != AnimalSpecies.BEES) {
            // Turn toward player
            val toPlayerX = playerPos.x - position.x
            val toPlayerZ = playerPos.z - position.z
            targetYaw = MathUtils.atan2(toPlayerX, toPlayerZ) * MathUtils.radiansToDegrees
        }

        if (aiState == AnimalAIState.WANDER) {
            val toTargetX = targetPos.x - position.x
            val toTargetZ = targetPos.z - position.z
            val dist = Math.sqrt((toTargetX * toTargetX + toTargetZ * toTargetZ).toDouble()).toFloat()

            if (dist > 0.2f) {
                targetYaw = MathUtils.atan2(toTargetX, toTargetZ) * MathUtils.radiansToDegrees
                val speed = if (species == AnimalSpecies.CHICKEN || species == AnimalSpecies.DUCK) 1.2f else 0.9f

                position.x += (toTargetX / dist) * speed * delta
                position.z += (toTargetZ / dist) * speed * delta

                // Clamp to paddock boundaries
                position.x = MathUtils.clamp(position.x, minX, maxX)
                position.z = MathUtils.clamp(position.z, minZ, maxZ)
            } else {
                aiState = AnimalAIState.EAT
                stateTimer = Random.nextFloat() * 4f + 3f
            }
        }

        // Smooth yaw rotation
        var diff = (targetYaw - yaw) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        yaw += diff * MathUtils.clamp(delta * 6f, 0f, 1f)

        // Terrain height follow
        position.y = FarmWorldPositionMapper.getTerrainHeight(position.x, position.z) + 0.1f

        // Update 3D ModelInstance transform if present
        instance?.let { inst ->
            inst.transform.setToTranslation(position.x, position.y, position.z)
            inst.transform.rotate(Vector3.Y, yaw)

            // Eating / drinking / idle animation bobbing
            if (aiState == AnimalAIState.EAT || aiState == AnimalAIState.DRINK) {
                val bob = Math.sin((System.currentTimeMillis() % 800) / 800.0 * Math.PI * 2.0).toFloat() * 0.04f
                inst.transform.trn(0f, bob, 0f)
            }
        }
    }

    private fun pickNextState() {
        if (species == AnimalSpecies.BEES) {
            aiState = AnimalAIState.IDLE
            stateTimer = 5f
            return
        }

        val r = Random.nextFloat()
        when {
            r < 0.40f -> {
                aiState = AnimalAIState.WANDER
                targetPos.set(
                    Random.nextFloat() * (maxX - minX) + minX,
                    0.1f,
                    Random.nextFloat() * (maxZ - minZ) + minZ
                )
                stateTimer = Random.nextFloat() * 5f + 3f
            }
            r < 0.70f -> {
                aiState = AnimalAIState.EAT
                stateTimer = Random.nextFloat() * 5f + 3f
            }
            r < 0.85f -> {
                aiState = AnimalAIState.DRINK
                stateTimer = Random.nextFloat() * 4f + 2f
            }
            else -> {
                aiState = AnimalAIState.REST
                stateTimer = Random.nextFloat() * 6f + 4f
            }
        }
    }
}
