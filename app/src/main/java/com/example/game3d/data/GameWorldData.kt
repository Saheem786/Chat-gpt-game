package com.example.game3d.data

import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.model.Season
import com.example.data.model.WeatherType

enum class InteractableType {
    CROP_PLOT,
    ANIMAL,
    WORKSHOP,
    ECO_SHOP,
    MARKET,
    SOLAR_STATION,
    FARM_HOUSE,
    COMPOSTER,
    NPC
}

data class InteractableTarget(
    val id: String,
    val type: InteractableType,
    val title: String,
    val subtitle: String,
    val distance: Float,
    val worldX: Float,
    val worldZ: Float,
    val primaryAction: String,
    val entityId: Long = 0L,
    val plotIndex: Int = 0
)

data class GameWorldSnapshot(
    val farmState: FarmStateEntity? = null,
    val animals: List<AnimalEntity> = emptyList(),
    val plots: List<CropPlotEntity> = emptyList(),
    val inventory: List<InventoryEntity> = emptyList(),
    val shelves: List<ShopShelfEntity> = emptyList(),
    val day: Int = 1,
    val hour: Int = 8,
    val season: Season = Season.SPRING,
    val weather: WeatherType = WeatherType.SUNNY,
    val solarEnergy: Float = 20f,
    val batteryStored: Float = 35f,
    val windTurbinesCount: Int = 1,
    val solarPanelsCount: Int = 2
)
