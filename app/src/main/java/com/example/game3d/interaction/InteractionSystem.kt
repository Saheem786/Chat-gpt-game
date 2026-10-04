package com.example.game3d.interaction

import com.badlogic.gdx.math.Vector3
import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.game3d.data.InteractableTarget
import com.example.game3d.data.InteractableType
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.world.Animal3DEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InteractionSystem {

    private val _currentTarget = MutableStateFlow<InteractableTarget?>(null)
    val currentTarget: StateFlow<InteractableTarget?> = _currentTarget.asStateFlow()

    // 8 Crop Plot World Centers
    val plotPositions = (1..8).map { id ->
        val index = id - 1
        val col = index % 2
        val row = index / 2
        val x = 9.0f + col * 4.5f
        val z = -4.0f + row * 4.5f
        id to Vector3(x, 0.1f, z)
    }.toMap()

    fun update(
        player: ThirdPersonPlayer,
        animals: List<Animal3DEntity>,
        rawAnimals: List<AnimalEntity>,
        plots: List<CropPlotEntity>
    ) {
        val pX = player.position.x
        val pZ = player.position.z

        var bestTarget: InteractableTarget? = null
        var bestDist = Float.MAX_VALUE

        // 1. Check Crop Plots
        for (plot in plots) {
            val pos = plotPositions[plot.id] ?: continue
            val dX = pos.x - pX
            val dZ = pos.z - pZ
            val dist = Math.sqrt((dX * dX + dZ * dZ).toDouble()).toFloat()

            if (dist < 3.2f && dist < bestDist) {
                val action = if (plot.cropType == null) {
                    "PLANT"
                } else if (plot.isReadyForHarvest) {
                    "HARVEST"
                } else if (plot.waterLevel < 0.5f) {
                    "WATER"
                } else {
                    "INSPECT"
                }

                val title = "Plot #${plot.id}: ${plot.cropType?.displayName ?: "Empty Soil"}"
                val subtitle = if (plot.cropType == null) {
                    "Ready for planting seeds"
                } else if (plot.isReadyForHarvest) {
                    "✨ Ready to harvest! (${plot.cropType?.baseYield} yield)"
                } else {
                    "Growing (${(plot.growthProgress * 100).toInt()}%) • Water: ${(plot.waterLevel * 100).toInt()}%"
                }

                bestTarget = InteractableTarget(
                    id = "plot_${plot.id}",
                    type = InteractableType.CROP_PLOT,
                    title = title,
                    subtitle = subtitle,
                    distance = dist,
                    worldX = pos.x,
                    worldZ = pos.z,
                    primaryAction = action,
                    plotIndex = plot.id
                )
                bestDist = dist
            }
        }

        // 2. Check 3D Animals
        for (animal in animals) {
            val dX = animal.position.x - pX
            val dZ = animal.position.z - pZ
            val dist = Math.sqrt((dX * dX + dZ * dZ).toDouble()).toFloat()

            if (dist < 3.0f && dist < bestDist) {
                val raw = rawAnimals.find { it.id == animal.entityId }
                val title = "${animal.nickname} (${animal.species.displayName})"
                val isReady = raw?.produceReady == true
                val subtitle = if (isReady) {
                    "🎁 Produce ready for collection!"
                } else {
                    "Health: ${(raw?.health?.times(100))?.toInt() ?: 100}% • Age: ${raw?.ageDays ?: 1}d"
                }

                val action = if (isReady) "COLLECT" else "INTERACT"

                bestTarget = InteractableTarget(
                    id = "animal_${animal.entityId}",
                    type = InteractableType.ANIMAL,
                    title = title,
                    subtitle = subtitle,
                    distance = dist,
                    worldX = animal.position.x,
                    worldZ = animal.position.z,
                    primaryAction = action,
                    entityId = animal.entityId
                )
                bestDist = dist
            }
        }

        // 3. Check Buildings
        val buildings = listOf(
            Triple(InteractableType.WORKSHOP, Vector3(-12f, 0f, -16f), "Solar Workshop" to "Craft cheese, flour, cloth & goods"),
            Triple(InteractableType.ECO_SHOP, Vector3(12f, 0f, -16f), "Eco-Shop Stall" to "Manage storefront shelves & prices"),
            Triple(InteractableType.MARKET, Vector3(0f, 0f, -23f), "Market & Fishery" to "Trade wholesale & complete contracts"),
            Triple(InteractableType.SOLAR_STATION, Vector3(0f, 0f, 19f), "Clean Energy Grid" to "Monitor solar panels & wind turbines"),
            Triple(InteractableType.COMPOSTER, Vector3(-6f, 0f, 19f), "Compost Digester" to "Convert manure into bio-fertilizer"),
            Triple(InteractableType.FARM_HOUSE, Vector3(0f, 0f, -10f), "Farmhouse" to "View enterprise business overview"),
            Triple(InteractableType.NPC, Vector3(4f, 0f, -20f), "Market Trader" to "Settlement trading partner")
        )

        for ((type, pos, texts) in buildings) {
            val dX = pos.x - pX
            val dZ = pos.z - pZ
            val dist = Math.sqrt((dX * dX + dZ * dZ).toDouble()).toFloat()

            if (dist < 4.2f && dist < bestDist) {
                val action = when (type) {
                    InteractableType.WORKSHOP -> "OPEN WORKSHOP"
                    InteractableType.ECO_SHOP -> "OPEN SHOP"
                    InteractableType.MARKET -> "TRADE"
                    InteractableType.SOLAR_STATION -> "INSPECT GRID"
                    InteractableType.COMPOSTER -> "COMPOST"
                    InteractableType.FARM_HOUSE -> "DASHBOARD"
                    InteractableType.NPC -> "TALK"
                    else -> "INTERACT"
                }

                bestTarget = InteractableTarget(
                    id = "building_${type.name}",
                    type = type,
                    title = texts.first,
                    subtitle = texts.second,
                    distance = dist,
                    worldX = pos.x,
                    worldZ = pos.z,
                    primaryAction = action
                )
                bestDist = dist
            }
        }

        _currentTarget.value = bestTarget
    }
}
