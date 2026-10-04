package com.example.game3d

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector3
import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.game3d.data.GameWorldSnapshot
import com.example.game3d.interaction.InteractionSystem
import com.example.game3d.player.PlayerInputState
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.ModelFactory
import com.example.game3d.renderer.WorldRenderer
import com.example.game3d.world.Animal3DEntity
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class SolarpunkGame(
    val inputState: PlayerInputState = PlayerInputState(),
    val interactionSystem: InteractionSystem = InteractionSystem(),
    private val onPlayerPositionChanged: ((x: Float, y: Float, z: Float, yaw: Float) -> Unit)? = null
) : ApplicationListener {

    private lateinit var camera: PerspectiveCamera
    private lateinit var thirdPersonCamera: ThirdPersonCamera
    lateinit var player: ThirdPersonPlayer
    private lateinit var modelFactory: ModelFactory
    private lateinit var renderer: WorldRenderer

    private val animalEntities = mutableListOf<Animal3DEntity>()
    private val latestSnapshot = AtomicReference<GameWorldSnapshot?>(null)

    private var currentRawAnimals = listOf<AnimalEntity>()
    private var currentPlots = listOf<CropPlotEntity>()

    private var posSaveTimer = 0f
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

        player = ThirdPersonPlayer(startX, startY, startZ, startYaw)
        thirdPersonCamera = ThirdPersonCamera(camera)

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

        // 1. Sync Animals
        val existingMap = animalEntities.associateBy { it.entityId }
        val updatedList = mutableListOf<Animal3DEntity>()

        for (raw in snapshot.animals) {
            val existing = existingMap[raw.id]
            if (existing != null) {
                existing.syncData(raw)
                updatedList.add(existing)
            } else {
                // Spawn new 3D animal in appropriate paddock area
                val model = modelFactory.createAnimalModel(raw.species)
                val instance = ModelInstance(model)

                val (spawnX, spawnZ) = when (raw.species) {
                    com.example.data.model.AnimalSpecies.CHICKEN, com.example.data.model.AnimalSpecies.DUCK ->
                        Pair(Random.nextFloat() * 4f - 19f, Random.nextFloat() * 6f + 8f)
                    com.example.data.model.AnimalSpecies.BEES ->
                        Pair(Random.nextFloat() * 4f + 13f, Random.nextFloat() * 4f + 7f)
                    else ->
                        Pair(Random.nextFloat() * 8f - 18f, Random.nextFloat() * 8f - 2f)
                }

                val new3DAnimal = Animal3DEntity(raw.id, raw.species, raw.nickname, spawnX, spawnZ, instance)
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
        if (snapshot != null && snapshot.animals !== currentRawAnimals || snapshot?.plots !== currentPlots) {
            if (snapshot != null) {
                syncStateData(snapshot)
            }
        }

        // 1. Update Player Movement & Procedural Limbs
        player.update(delta, inputState, thirdPersonCamera.yaw)

        // 2. Update Camera Follow & Orbit
        thirdPersonCamera.update(delta, player, inputState)
        inputState.resetDeltas()

        // 3. Update Animal AI
        for (animal in animalEntities) {
            animal.update(delta, player.position)
        }

        // 4. Update Lighting, Weather & Time
        val hour = snapshot?.hour ?: 8
        val weather = snapshot?.weather ?: com.example.data.model.WeatherType.SUNNY
        renderer.updateLightingAndTime(hour, weather, delta)

        // 5. Update Interaction Raycast Solver
        interactionSystem.update(player, animalEntities, currentRawAnimals, currentPlots)

        // 6. Periodically save player position
        posSaveTimer += delta
        if (posSaveTimer >= 2.0f) {
            posSaveTimer = 0f
            onPlayerPositionChanged?.invoke(player.position.x, player.position.y, player.position.z, player.yaw)
        }

        // 7. Render 3D Scene
        renderer.render(camera, player, animalEntities)
    }

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {
        if (isInitialized) {
            renderer.dispose()
        }
    }
}
