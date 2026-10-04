package com.example.game3d.world.mapper

import com.badlogic.gdx.math.Vector3
import com.example.data.model.AnimalSpecies

object FarmWorldPositionMapper {

    // Centralized Crop Plot Grid Positioning
    // Supports 8 default plots and extensible for future plots
    fun getPlotPosition(plotId: Int): Vector3 {
        val index = plotId - 1
        val col = index % 2
        val row = index / 2
        val x = 9.0f + col * 4.5f
        val z = -4.0f + row * 4.5f
        val y = getTerrainHeight(x, z)
        return Vector3(x, y + 0.12f, z)
    }

    // Centralized Building Positions
    val FARM_HOUSE_POS = Vector3(0f, 1.75f, -13f)
    val BARN_POS = Vector3(-18f, 2.25f, 0f)
    val CHICKEN_COOP_POS = Vector3(-18f, 1.25f, 12f)
    val WORKSHOP_POS = Vector3(-12f, 1.9f, -18f)
    val ECO_SHOP_POS = Vector3(12f, 1.6f, -18f)
    val MARKET_DOCK_POS = Vector3(0f, 0.2f, -25f)
    val GREENHOUSE_POS = Vector3(14f, 1.5f, 16f)
    val RAIN_TOWER_POS = Vector3(0f, 1.5f, 21f)
    val COMPOSTER_POS = Vector3(-6f, 1.0f, 21f)
    val SOLAR_ARRAY_POS = Vector3(6f, 0.6f, 21f)
    val WIND_TURBINE_POS = Vector3(12f, 4.25f, 24f)
    val POND_POS = Vector3(-15f, 0.05f, 20f)

    // Centralized NPC Positions
    val NPC_TRADER_POS = Vector3(4f, 0.6f, -20f)
    val NPC_CHEF_POS = Vector3(-4f, 0.6f, -20f)

    // Deterministic Initial Animal Spawns based on species/paddock zones
    fun getInitialAnimalSpawn(species: AnimalSpecies, animalIndex: Int): Vector3 {
        val y = getTerrainHeight(0f, 0f)
        return when (species) {
            AnimalSpecies.CHICKEN -> {
                val offsetX = (animalIndex % 3) * 1.5f - 18.5f
                val offsetZ = (animalIndex / 3) * 1.5f + 9.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
            AnimalSpecies.DUCK -> {
                val offsetX = (animalIndex % 2) * 1.8f - 16.0f
                val offsetZ = (animalIndex / 2) * 1.8f + 13.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
            AnimalSpecies.BEES -> {
                val offsetX = (animalIndex % 2) * 2.0f + 14.0f
                val offsetZ = (animalIndex / 2) * 2.0f + 8.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
            AnimalSpecies.COW -> {
                val offsetX = (animalIndex % 2) * 3.5f - 16.0f
                val offsetZ = (animalIndex / 2) * 3.5f - 2.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
            AnimalSpecies.GOAT -> {
                val offsetX = (animalIndex % 2) * 2.5f - 12.0f
                val offsetZ = (animalIndex / 2) * 2.5f + 2.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
            AnimalSpecies.SHEEP -> {
                val offsetX = (animalIndex % 2) * 2.5f - 18.0f
                val offsetZ = (animalIndex / 2) * 2.5f + 3.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
            AnimalSpecies.PIG -> {
                val offsetX = (animalIndex % 2) * 2.5f - 10.0f
                val offsetZ = (animalIndex / 2) * 2.5f - 3.0f
                Vector3(offsetX, y + 0.1f, offsetZ)
            }
        }
    }

    // Terrain Height Query Function
    // Current terrain has ground baseline at 0.0f, with support for future procedural heightmaps or ramps
    fun getTerrainHeight(x: Float, z: Float): Float {
        // Base ground level is 0.0f
        return 0.0f
    }
}
