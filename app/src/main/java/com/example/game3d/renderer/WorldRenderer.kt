package com.example.game3d.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.example.data.local.CropPlotEntity
import com.example.data.model.WeatherType
import com.example.game3d.data.GameWorldSnapshot
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.world.Animal3DEntity

class WorldRenderer(
    private val modelFactory: ModelFactory
) {
    lateinit var modelBatch: ModelBatch
    lateinit var environment: Environment
    private lateinit var dirLight: DirectionalLight

    // Static World Instances
    private lateinit var terrainInstance: ModelInstance
    private lateinit var pondInstance: ModelInstance
    private val staticInstances = mutableListOf<ModelInstance>()

    // Wind Turbine Propeller Instance for Rotation
    private lateinit var turbineBladesInstance: ModelInstance
    private var turbineRotation: Float = 0f

    // Dynamic Instances
    private val cropPlotInstances = mutableListOf<ModelInstance>()
    private val cropPlantInstances = mutableListOf<ModelInstance>()

    // Player Components
    private lateinit var playerTorsoInstance: ModelInstance
    private lateinit var playerHeadInstance: ModelInstance
    private lateinit var playerLeftArmInstance: ModelInstance
    private lateinit var playerRightArmInstance: ModelInstance
    private lateinit var playerLeftLegInstance: ModelInstance
    private lateinit var playerRightLegInstance: ModelInstance

    // Sky Clear Colors
    private val skyDay = Color(0.48f, 0.72f, 0.92f, 1f)
    private val skySunset = Color(0.85f, 0.52f, 0.38f, 1f)
    private val skyNight = Color(0.06f, 0.08f, 0.16f, 1f)
    private val currentSkyColor = Color()

    fun create() {
        modelBatch = ModelBatch()
        environment = Environment()

        dirLight = DirectionalLight().set(0.9f, 0.9f, 0.85f, -0.4f, -0.8f, -0.4f)
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.48f, 1f))
        environment.add(dirLight)

        buildStaticWorld()
        buildPlayerInstances()
    }

    private fun buildStaticWorld() {
        // 1. Terrain & Water
        terrainInstance = ModelInstance(modelFactory.createTerrain())
        terrainInstance.transform.setToTranslation(0f, -0.1f, 0f)

        pondInstance = ModelInstance(modelFactory.createWaterPond())
        pondInstance.transform.setToTranslation(-15f, 0.05f, 20f)
        staticInstances.add(pondInstance)

        // 2. Stone Pathways
        val mainPath = ModelInstance(modelFactory.createStonePath(3.5f, 40f))
        mainPath.transform.setToTranslation(0f, 0.02f, -2f)
        staticInstances.add(mainPath)

        val crossPath = ModelInstance(modelFactory.createStonePath(30f, 3.5f))
        crossPath.transform.setToTranslation(0f, 0.02f, -16f)
        staticInstances.add(crossPath)

        // 3. Buildings
        val farmHouse = ModelInstance(modelFactory.createFarmHouse())
        farmHouse.transform.setToTranslation(0f, 1.75f, -13f)
        staticInstances.add(farmHouse)

        val barn = ModelInstance(modelFactory.createBarn())
        barn.transform.setToTranslation(-18f, 2.25f, 0f)
        staticInstances.add(barn)

        val coop = ModelInstance(modelFactory.createChickenCoop())
        coop.transform.setToTranslation(-18f, 1.25f, 12f)
        staticInstances.add(coop)

        val workshop = ModelInstance(modelFactory.createWorkshopBuilding())
        workshop.transform.setToTranslation(-12f, 1.9f, -18f)
        staticInstances.add(workshop)

        val shop = ModelInstance(modelFactory.createEcoShopBuilding())
        shop.transform.setToTranslation(12f, 1.6f, -18f)
        staticInstances.add(shop)

        val market = ModelInstance(modelFactory.createMarketDock())
        market.transform.setToTranslation(0f, 0.2f, -25f)
        staticInstances.add(market)

        val greenhouse = ModelInstance(modelFactory.createGreenhouse())
        greenhouse.transform.setToTranslation(14f, 1.5f, 16f)
        staticInstances.add(greenhouse)

        val rainTower = ModelInstance(modelFactory.createRainTower())
        rainTower.transform.setToTranslation(0f, 1.5f, 21f)
        staticInstances.add(rainTower)

        val composter = ModelInstance(modelFactory.createComposterDigester())
        composter.transform.setToTranslation(-6f, 1.0f, 21f)
        staticInstances.add(composter)

        val solarArray = ModelInstance(modelFactory.createSolarPanelArray())
        solarArray.transform.setToTranslation(6f, 0.6f, 21f)
        solarArray.transform.rotate(Vector3.X, 25f)
        staticInstances.add(solarArray)

        // Wind Turbine
        val turbineTower = ModelInstance(modelFactory.createWindTurbineTower())
        turbineTower.transform.setToTranslation(12f, 4.25f, 24f)
        staticInstances.add(turbineTower)

        turbineBladesInstance = ModelInstance(modelFactory.createWindTurbineBlades())
        turbineBladesInstance.transform.setToTranslation(12f, 8.5f, 23.3f)

        // Trees & Nature around perimeter
        val treeCoords = listOf(
            Triple(-28f, -20f, true), Triple(-25f, -10f, false), Triple(-28f, 5f, true),
            Triple(-28f, 20f, false), Triple(25f, -20f, false), Triple(28f, -5f, true),
            Triple(28f, 10f, false), Triple(25f, 22f, true), Triple(-8f, 28f, true),
            Triple(8f, 28f, false), Triple(-15f, -28f, false), Triple(15f, -28f, true)
        )
        for ((tx, tz, isPine) in treeCoords) {
            val tree = ModelInstance(modelFactory.createTree(isPine))
            tree.transform.setToTranslation(tx, 0f, tz)
            staticInstances.add(tree)
        }

        // NPCs in Market
        val npcTrader = ModelInstance(modelFactory.createNPC("MERCHANT"))
        npcTrader.transform.setToTranslation(4f, 0.6f, -20f)
        staticInstances.add(npcTrader)

        val npcChef = ModelInstance(modelFactory.createNPC("CHEF"))
        npcChef.transform.setToTranslation(-4f, 0.6f, -20f)
        staticInstances.add(npcChef)

        // 8 Crop Plot Bed Meshes
        val plotBedModel = modelFactory.createCropPlotBed()
        (1..8).forEach { id ->
            val index = id - 1
            val col = index % 2
            val row = index / 2
            val x = 9.0f + col * 4.5f
            val z = -4.0f + row * 4.5f

            val bed = ModelInstance(plotBedModel)
            bed.transform.setToTranslation(x, 0.12f, z)
            cropPlotInstances.add(bed)
        }
    }

    private fun buildPlayerInstances() {
        playerTorsoInstance = ModelInstance(modelFactory.createPlayerBody())
        playerHeadInstance = ModelInstance(modelFactory.createPlayerHead())
        playerLeftArmInstance = ModelInstance(modelFactory.createPlayerLimb(true))
        playerRightArmInstance = ModelInstance(modelFactory.createPlayerLimb(true))
        playerLeftLegInstance = ModelInstance(modelFactory.createPlayerLimb(false))
        playerRightLegInstance = ModelInstance(modelFactory.createPlayerLimb(false))
    }

    fun syncCropPlots(plots: List<CropPlotEntity>) {
        cropPlantInstances.clear()
        for (plot in plots) {
            val cropType = plot.cropType ?: continue
            val index = plot.id - 1
            val col = index % 2
            val row = index / 2
            val x = 9.0f + col * 4.5f
            val z = -4.0f + row * 4.5f

            val plantModel = modelFactory.createCropModel(cropType, plot.isReadyForHarvest)
            val plantInstance = ModelInstance(plantModel)
            plantInstance.transform.setToTranslation(x, 0.25f, z)
            cropPlantInstances.add(plantInstance)
        }
    }

    fun updateLightingAndTime(hour: Int, weather: WeatherType, delta: Float) {
        // Wind turbine rotation
        val windMultiplier = weather.windMultiplier
        turbineRotation += delta * 140f * windMultiplier
        turbineBladesInstance.transform.setToTranslation(12f, 8.5f, 23.3f)
        turbineBladesInstance.transform.rotate(Vector3.Z, turbineRotation)

        // Day/Night & Lighting
        when (hour) {
            in 6..17 -> {
                // Daytime
                currentSkyColor.set(skyDay)
                dirLight.set(0.95f, 0.95f, 0.90f, -0.4f, -0.8f, -0.4f)
                environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.48f, 1f))
            }
            in 18..20 -> {
                // Sunset
                currentSkyColor.set(skySunset)
                dirLight.set(0.95f, 0.55f, 0.35f, -0.7f, -0.4f, -0.3f)
                environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.35f, 0.28f, 0.32f, 1f))
            }
            else -> {
                // Nighttime
                currentSkyColor.set(skyNight)
                dirLight.set(0.20f, 0.25f, 0.45f, -0.3f, -0.9f, -0.3f)
                environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.15f, 0.18f, 0.25f, 1f))
            }
        }
    }

    fun render(
        camera: PerspectiveCamera,
        player: ThirdPersonPlayer,
        animals: List<Animal3DEntity>
    ) {
        Gdx.gl.glClearColor(currentSkyColor.r, currentSkyColor.g, currentSkyColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        updatePlayerTransforms(player)

        modelBatch.begin(camera)

        // 1. Terrain & Static Environment
        modelBatch.render(terrainInstance, environment)
        for (staticInst in staticInstances) {
            modelBatch.render(staticInst, environment)
        }
        modelBatch.render(turbineBladesInstance, environment)

        // 2. Crop Plots & Plants
        for (bed in cropPlotInstances) {
            modelBatch.render(bed, environment)
        }
        for (plant in cropPlantInstances) {
            modelBatch.render(plant, environment)
        }

        // 3. Animals
        for (animal in animals) {
            modelBatch.render(animal.instance, environment)
        }

        // 4. Player Character
        modelBatch.render(playerTorsoInstance, environment)
        modelBatch.render(playerHeadInstance, environment)
        modelBatch.render(playerLeftArmInstance, environment)
        modelBatch.render(playerRightArmInstance, environment)
        modelBatch.render(playerLeftLegInstance, environment)
        modelBatch.render(playerRightLegInstance, environment)

        modelBatch.end()
    }

    private fun updatePlayerTransforms(player: ThirdPersonPlayer) {
        val px = player.position.x
        val py = player.position.y + player.bodyBobY
        val pz = player.position.z
        val yaw = player.yaw

        // Torso
        playerTorsoInstance.transform.setToTranslation(px, py + 1.0f, pz)
        playerTorsoInstance.transform.rotate(Vector3.Y, yaw)

        // Head
        playerHeadInstance.transform.setToTranslation(px, py + 1.55f, pz)
        playerHeadInstance.transform.rotate(Vector3.Y, yaw)

        // Left Arm
        playerLeftArmInstance.transform.setToTranslation(px, py + 1.05f, pz)
        playerLeftArmInstance.transform.rotate(Vector3.Y, yaw)
        playerLeftArmInstance.transform.translate(-0.35f, 0f, 0f)
        playerLeftArmInstance.transform.rotate(Vector3.X, player.leftArmAngle)

        // Right Arm
        playerRightArmInstance.transform.setToTranslation(px, py + 1.05f, pz)
        playerRightArmInstance.transform.rotate(Vector3.Y, yaw)
        playerRightArmInstance.transform.translate(0.35f, 0f, 0f)
        playerRightArmInstance.transform.rotate(Vector3.X, player.rightArmAngle)

        // Left Leg
        playerLeftLegInstance.transform.setToTranslation(px, py + 0.45f, pz)
        playerLeftLegInstance.transform.rotate(Vector3.Y, yaw)
        playerLeftLegInstance.transform.translate(-0.16f, 0f, 0f)
        playerLeftLegInstance.transform.rotate(Vector3.X, player.leftLegAngle)

        // Right Leg
        playerRightLegInstance.transform.setToTranslation(px, py + 0.45f, pz)
        playerRightLegInstance.transform.rotate(Vector3.Y, yaw)
        playerRightLegInstance.transform.translate(0.16f, 0f, 0f)
        playerRightLegInstance.transform.rotate(Vector3.X, player.rightLegAngle)
    }

    fun dispose() {
        modelBatch.dispose()
    }
}
