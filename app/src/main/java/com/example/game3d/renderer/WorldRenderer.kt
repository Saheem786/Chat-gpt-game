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
import com.badlogic.gdx.math.Vector3
import com.example.data.local.CropPlotEntity
import com.example.data.model.WeatherType
import com.example.game3d.collision.WorldCollisionSystem
import com.example.game3d.player.IPlayerVisual
import com.example.game3d.player.ProceduralPlayerVisual
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.world.Animal3DEntity
import com.example.game3d.world.mapper.FarmWorldPositionMapper

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

    // Dynamic Crop Instances
    private val cropPlotInstances = mutableListOf<ModelInstance>()
    private val cropPlantInstances = mutableListOf<ModelInstance>()

    // Player Visual (Abstracted for future GLB replacement)
    lateinit var playerVisual: IPlayerVisual

    // Smooth 24-Hour Day/Night Lighting System
    val dayNightSystem = DayNightLightingSystem()

    fun create() {
        modelBatch = ModelBatch()
        environment = Environment()

        dirLight = DirectionalLight().set(0.9f, 0.9f, 0.85f, -0.4f, -0.8f, -0.4f)
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.48f, 1f))
        environment.add(dirLight)

        buildStaticWorld()
        playerVisual = ProceduralPlayerVisual(modelFactory)
    }

    private fun buildStaticWorld() {
        // 1. Terrain & Water
        terrainInstance = ModelInstance(modelFactory.createTerrain())
        terrainInstance.transform.setToTranslation(0f, -0.1f, 0f)

        pondInstance = ModelInstance(modelFactory.createWaterPond())
        pondInstance.transform.setToTranslation(FarmWorldPositionMapper.POND_POS)
        staticInstances.add(pondInstance)

        // 2. Stone Pathways
        val mainPath = ModelInstance(modelFactory.createStonePath(3.5f, 40f))
        mainPath.transform.setToTranslation(0f, 0.02f, -2f)
        staticInstances.add(mainPath)

        val crossPath = ModelInstance(modelFactory.createStonePath(30f, 3.5f))
        crossPath.transform.setToTranslation(0f, 0.02f, -16f)
        staticInstances.add(crossPath)

        // 3. Buildings & Farm Structures
        val farmHouse = ModelInstance(modelFactory.createFarmHouse())
        farmHouse.transform.setToTranslation(FarmWorldPositionMapper.FARM_HOUSE_POS)
        staticInstances.add(farmHouse)

        val barn = ModelInstance(modelFactory.createBarn())
        barn.transform.setToTranslation(FarmWorldPositionMapper.BARN_POS)
        staticInstances.add(barn)

        val coop = ModelInstance(modelFactory.createChickenCoop())
        coop.transform.setToTranslation(FarmWorldPositionMapper.CHICKEN_COOP_POS)
        staticInstances.add(coop)

        val workshop = ModelInstance(modelFactory.createWorkshopBuilding())
        workshop.transform.setToTranslation(FarmWorldPositionMapper.WORKSHOP_POS)
        staticInstances.add(workshop)

        val shop = ModelInstance(modelFactory.createEcoShopBuilding())
        shop.transform.setToTranslation(FarmWorldPositionMapper.ECO_SHOP_POS)
        staticInstances.add(shop)

        val market = ModelInstance(modelFactory.createMarketDock())
        market.transform.setToTranslation(FarmWorldPositionMapper.MARKET_DOCK_POS)
        staticInstances.add(market)

        val greenhouse = ModelInstance(modelFactory.createGreenhouse())
        greenhouse.transform.setToTranslation(FarmWorldPositionMapper.GREENHOUSE_POS)
        staticInstances.add(greenhouse)

        val rainTower = ModelInstance(modelFactory.createRainTower())
        rainTower.transform.setToTranslation(FarmWorldPositionMapper.RAIN_TOWER_POS)
        staticInstances.add(rainTower)

        val composter = ModelInstance(modelFactory.createComposterDigester())
        composter.transform.setToTranslation(FarmWorldPositionMapper.COMPOSTER_POS)
        staticInstances.add(composter)

        val solarArray = ModelInstance(modelFactory.createSolarPanelArray())
        solarArray.transform.setToTranslation(FarmWorldPositionMapper.SOLAR_ARRAY_POS)
        solarArray.transform.rotate(Vector3.X, 25f)
        staticInstances.add(solarArray)

        // Wind Turbine
        val turbineTower = ModelInstance(modelFactory.createWindTurbineTower())
        turbineTower.transform.setToTranslation(FarmWorldPositionMapper.WIND_TURBINE_POS)
        staticInstances.add(turbineTower)

        turbineBladesInstance = ModelInstance(modelFactory.createWindTurbineBlades())
        turbineBladesInstance.transform.setToTranslation(12f, 8.5f, 23.3f)

        // Trees & Nature around perimeter
        for ((tx, tz, isPine) in WorldCollisionSystem.treeCoords) {
            val tree = ModelInstance(modelFactory.createTree(isPine))
            tree.transform.setToTranslation(tx, FarmWorldPositionMapper.getTerrainHeight(tx, tz), tz)
            staticInstances.add(tree)
        }

        // NPCs in Market
        val npcTrader = ModelInstance(modelFactory.createNPC("MERCHANT"))
        npcTrader.transform.setToTranslation(FarmWorldPositionMapper.NPC_TRADER_POS)
        staticInstances.add(npcTrader)

        val npcChef = ModelInstance(modelFactory.createNPC("CHEF"))
        npcChef.transform.setToTranslation(FarmWorldPositionMapper.NPC_CHEF_POS)
        staticInstances.add(npcChef)

        // 8 Crop Plot Bed Meshes
        val plotBedModel = modelFactory.createCropPlotBed()
        (1..8).forEach { id ->
            val bedPos = FarmWorldPositionMapper.getPlotPosition(id)
            val bed = ModelInstance(plotBedModel)
            bed.transform.setToTranslation(bedPos)
            cropPlotInstances.add(bed)
        }
    }

    fun syncCropPlots(plots: List<CropPlotEntity>) {
        cropPlantInstances.clear()
        for (plot in plots) {
            val cropType = plot.cropType ?: continue
            val plotPos = FarmWorldPositionMapper.getPlotPosition(plot.id)

            val plantModel = modelFactory.createCropModel(cropType, plot.isReadyForHarvest)
            val plantInstance = ModelInstance(plantModel)
            plantInstance.transform.setToTranslation(plotPos.x, plotPos.y + 0.15f, plotPos.z)
            cropPlantInstances.add(plantInstance)
        }
    }

    fun updateLightingAndTime(timeOfDay: Float, weather: WeatherType, delta: Float) {
        val windMultiplier = weather.windMultiplier
        turbineRotation += delta * 140f * windMultiplier
        turbineBladesInstance.transform.setToTranslation(12f, 8.5f, 23.3f)
        turbineBladesInstance.transform.rotate(Vector3.Z, turbineRotation)

        dayNightSystem.update(timeOfDay, weather, delta)

        // Update dynamic directional sun/moon light
        dirLight.set(
            dayNightSystem.currentSunColor.r,
            dayNightSystem.currentSunColor.g,
            dayNightSystem.currentSunColor.b,
            dayNightSystem.sunDirection.x,
            dayNightSystem.sunDirection.y,
            dayNightSystem.sunDirection.z
        )

        // Update ambient illumination
        environment.set(
            ColorAttribute(
                ColorAttribute.AmbientLight,
                dayNightSystem.currentAmbientColor.r,
                dayNightSystem.currentAmbientColor.g,
                dayNightSystem.currentAmbientColor.b,
                1f
            )
        )
    }

    fun render(
        camera: PerspectiveCamera,
        player: ThirdPersonPlayer,
        animals: List<Animal3DEntity>,
        delta: Float
    ) {
        val sky = dayNightSystem.currentSkyColor
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        // Update player visual animation
        playerVisual.updateAnimation(delta, player.isMoving, player.isRunning, player.walkTimer)

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

        // 4. Render Player Character
        playerVisual.render(modelBatch, environment, player.position, player.yaw)

        modelBatch.end()
    }

    fun dispose() {
        modelBatch.dispose()
        playerVisual.dispose()
    }
}
