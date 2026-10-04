package com.example.game3d

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector3
import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.game3d.audio.SpatialLivestockAudioSystem
import com.example.game3d.data.GameWorldSnapshot
import com.example.game3d.interaction.InteractionSystem
import com.example.game3d.player.PlayerInputState
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.ModelFactory
import com.example.game3d.renderer.WorldRenderer
import com.example.game3d.world.Animal3DEntity
import com.example.game3d.world.mapper.FarmWorldPositionMapper
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class SolarpunkGame(
    val inputState: PlayerInputState = PlayerInputState(),
    val interactionSystem: InteractionSystem = InteractionSystem(),
    val spatialAudio: SpatialLivestockAudioSystem = SpatialLivestockAudioSystem(),
    private val onPlayerAndCameraStateChanged: ((x: Float, y: Float, z: Float, yaw: Float, camYaw: Float, camPitch: Float, camDist: Float) -> Unit)? = null,
    private val onAnimalPositionChanged: ((animalId: Long, x: Float, y: Float, z: Float, yaw: Float) -> Unit)? = null
) : ApplicationListener {

    private lateinit var camera: PerspectiveCamera
    lateinit var thirdPersonCamera: ThirdPersonCamera
    lateinit var player: ThirdPersonPlayer
    private lateinit var modelFactory: ModelFactory
    private lateinit var renderer: WorldRenderer

    private val animalEntities = mutableListOf<Animal3DEntity>()
    private val latestSnapshot = AtomicReference<GameWorldSnapshot?>(null)

    private var currentRawAnimals = listOf<AnimalEntity>()
    private var currentPlots = listOf<CropPlotEntity>()

    private var stateSaveTimer = 0f
    private var animalSaveTimer = 0f
    private var isInitialized = false

    fun updateWorldSnapshot(snapshot: GameWorldSnapshot) {
        latestSnapshot.set(snapshot)
    }

    override fun create() {
        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            near = 0.1f
            far = 250f
            position.set(0f, 4f, -10f)
            lookAt(0f, 1f, -6f)
            update()
        }

        val snapshot = latestSnapshot.get()
        val startX = snapshot?.farmState?.playerX ?: 0f
        val startY = snapshot?.farmState?.playerY ?: 0f
        val startZ = snapshot?.farmState?.playerZ ?: -6f
        val startYaw = snapshot?.farmState?.playerYaw ?: 180f

        val initialCamYaw = snapshot?.farmState?.cameraYaw ?: 180f
        val initialCamPitch = snapshot?.farmState?.cameraPitch ?: 22f
        val initialCamDist = snapshot?.farmState?.cameraDistance ?: 5.2f

        player = ThirdPersonPlayer(startX, startY, startZ, startYaw)
        thirdPersonCamera = ThirdPersonCamera(camera, initialCamYaw, initialCamPitch, initialCamDist)

        modelFactory = ModelFactory()
        renderer = WorldRenderer(modelFactory)
        renderer.create()

        if (snapshot != null) {
            syncStateData(snapshot)
        }

        isInitialized = true
    }

    private fun syncStateData(snapshot: GameWorldSnapshot) {
        currentRawAnimals = snapshot.animals
        currentPlots = snapshot.plots

        // 1. Sync Animals with position persistence
        val existingMap = animalEntities.associateBy { it.entityId }
        val updatedList = mutableListOf<Animal3DEntity>()

        for ((index, raw) in snapshot.animals.withIndex()) {
            val existing = existingMap[raw.id]
            if (existing != null) {
                existing.syncData(raw)
                updatedList.add(existing)
            } else {
                // Determine spawn coordinates (restore saved or use deterministic paddock spawn)
                val hasSavedPos = raw.worldX != 0f || raw.worldZ != 0f
                val spawnPos = if (hasSavedPos) {
                    Vector3(raw.worldX, raw.worldY, raw.worldZ)
                } else {
                    FarmWorldPositionMapper.getInitialAnimalSpawn(raw.species, index)
                }
                val spawnYaw = if (hasSavedPos) raw.worldYaw else (raw.id * 57f) % 360f

                val model = modelFactory.createAnimalModel(raw.species)
                val instance = ModelInstance(model)

                val new3DAnimal = Animal3DEntity(
                    entityId = raw.id,
                    species = raw.species,
                    nickname = raw.nickname,
                    initialX = spawnPos.x,
                    initialY = spawnPos.y,
                    initialZ = spawnPos.z,
                    initialYaw = spawnYaw,
                    instance = instance
                )
                updatedList.add(new3DAnimal)
            }
        }

        animalEntities.clear()
        animalEntities.addAll(updatedList)

        // 2. Sync Crop Plots
        renderer.syncCropPlots(snapshot.plots)
    }

    override fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            camera.viewportWidth = width.toFloat()
            camera.viewportHeight = height.toFloat()
            camera.update()
        }
    }

    override fun render() {
        val delta = Math.min(Gdx.graphics.deltaTime, 0.1f)

        // Check if new snapshot arrived from Room/ViewModel
        val snapshot = latestSnapshot.get()
        if (snapshot != null && (snapshot.animals !== currentRawAnimals || snapshot.plots !== currentPlots)) {
            syncStateData(snapshot)
        }

        // 1. Update Player Movement & Collision
        player.update(delta, inputState, thirdPersonCamera.yaw)

        // 2. Update Camera Follow & Obstruction Avoidance
        thirdPersonCamera.update(delta, player, inputState)
        inputState.resetDeltas()

        // 3. Update Animal AI
        for (animal in animalEntities) {
            animal.update(delta, player.position)
        }

        // 4. Update Spatial Livestock Audio
        spatialAudio.update(delta, player.position, player.yaw, animalEntities)

        // 5. Update Lighting, Weather & Time
        val hour = snapshot?.hour ?: 8
        val weather = snapshot?.weather ?: com.example.data.model.WeatherType.SUNNY
        renderer.updateLightingAndTime(hour, weather, delta)

        // 6. Update Interaction Raycast Solver
        interactionSystem.update(player, animalEntities, currentRawAnimals, currentPlots)

        // 7. Periodically persist player & camera state
        stateSaveTimer += delta
        if (stateSaveTimer >= 2.0f) {
            stateSaveTimer = 0f
            onPlayerAndCameraStateChanged?.invoke(
                player.position.x,
                player.position.y,
                player.position.z,
                player.yaw,
                thirdPersonCamera.yaw,
                thirdPersonCamera.pitch,
                thirdPersonCamera.desiredDistance
            )
        }

        // 8. Periodically persist animal world positions
        animalSaveTimer += delta
        if (animalSaveTimer >= 5.0f) {
            animalSaveTimer = 0f
            for (animal in animalEntities) {
                onAnimalPositionChanged?.invoke(
                    animal.entityId,
                    animal.position.x,
                    animal.position.y,
                    animal.position.z,
                    animal.yaw
                )
            }
        }

        // 9. Render 3D Scene
        renderer.render(camera, player, animalEntities, delta)
    }

    fun playAnimalInteractionSound(animalId: Long) {
        val animal = animalEntities.find { it.entityId == animalId }
        if (animal != null) {
            spatialAudio.playInteractionSound(animal.species, animal.position, player.position, player.yaw)
        }
    }

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {
        spatialAudio.dispose()
        if (isInitialized) {
            renderer.dispose()
        }
    }
}
