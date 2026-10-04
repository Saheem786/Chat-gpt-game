package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AnimalSpecies
import com.example.data.model.CropType
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.data.model.Season
import com.example.data.model.SettlementTier
import com.example.data.model.WeatherType
import com.example.data.model.BusinessLevel

@Entity(tableName = "farm_state")
data class FarmStateEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 350,
    val ecoHarmonyScore: Int = 45,
    val day: Int = 1,
    val hour: Int = 8,
    val season: Season = Season.SPRING,
    val weather: WeatherType = WeatherType.SUNNY,
    val weatherHoursRemaining: Int = 8,
    val solarEnergy: Float = 15f,
    val batteryStored: Float = 25f,
    val batteryMax: Float = 50f,
    val waterStored: Float = 60f,
    val waterMax: Float = 100f,
    val compostBinsActive: Int = 1,
    val compostProgress: Float = 0f,
    val settlementTier: SettlementTier = SettlementTier.HOMESTEAD,
    val totalEarnings: Long = 0,
    val fishPopulationHealth: Float = 0.95f,
    val aquaponicsActive: Boolean = false,
    val solarPanelsCount: Int = 2,
    val windTurbinesCount: Int = 1,
    val rainCollectorsCount: Int = 2,
    // Business Subsystem Metrics
    val totalExpenses: Long = 0,
    val salesToday: Int = 0,
    val livestockSoldTotal: Int = 0,
    val meatProcessedTotal: Int = 0,
    val wholesaleIncomeTotal: Long = 0,
    val businessReputation: Int = 50,
    val businessLevel: BusinessLevel = BusinessLevel.LEVEL_1,
    // 3D Player Persistent Position
    val playerX: Float = 0f,
    val playerY: Float = 0f,
    val playerZ: Float = -6f,
    val playerYaw: Float = 180f,
    // 3D Camera Persistent State
    val cameraYaw: Float = 180f,
    val cameraPitch: Float = 22f,
    val cameraDistance: Float = 5.2f
)

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val species: AnimalSpecies,
    val nickname: String,
    val hunger: Float = 0.1f, // 0 is fed, 1 is starving
    val thirst: Float = 0.1f,
    val health: Float = 1.0f,
    val happiness: Float = 0.9f,
    val ageDays: Int = 1,
    val produceReady: Boolean = false,
    val hoursUntilProduce: Int = 6,
    val isPregnant: Boolean = false,
    val pregnancyHours: Int = 0,
    // 3D Persistent Animal World Coordinates
    val worldX: Float = 0f,
    val worldY: Float = 0f,
    val worldZ: Float = 0f,
    val worldYaw: Float = 0f
)

@Entity(tableName = "crop_plots")
data class CropPlotEntity(
    @PrimaryKey val id: Int, // 1 through 12
    val cropType: CropType? = null,
    val growthProgress: Float = 0f, // 0.0 to 1.0
    val waterLevel: Float = 0.8f, // 0.0 to 1.0
    val soilFertility: Float = 1.0f, // 0.5 to 1.8 boosted by compost
    val hasGreenhouse: Boolean = false,
    val isAutomatedDrip: Boolean = false,
    val isReadyForHarvest: Boolean = false
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: ItemId,
    val quantity: Int = 0
)

@Entity(tableName = "workshop_queue")
data class WorkshopQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: String,
    val progressHours: Float = 0f,
    val totalHours: Int = 4,
    val isFinished: Boolean = false
)

@Entity(tableName = "shop_shelves")
data class ShopShelfEntity(
    @PrimaryKey val shelfId: Int, // 1 to 6
    val stockedItemId: ItemId? = null,
    val quantity: Int = 0,
    val pricingStrategy: PricingStrategy = PricingStrategy.FAIR_TRADE
)

@Entity(tableName = "npc_contracts")
data class ContractEntity(
    @PrimaryKey val id: String,
    val clientName: String,
    val clientAvatarEmoji: String,
    val clientRole: String,
    val requestedItem: ItemId,
    val requestedQuantity: Int,
    val rewardCoins: Int,
    val rewardEcoScore: Int,
    val expiryDay: Int,
    val isCompleted: Boolean = false,
    val isPenalized: Boolean = false
)

@Entity(tableName = "market_quotes")
data class MarketQuoteEntity(
    @PrimaryKey val itemId: ItemId,
    val basePrice: Int,
    val currentPrice: Int,
    val priceChangePercent: Int,
    val demand: com.example.data.model.MarketDemand,
    val marketDriver: String,
    val dayCalculated: Int
)

@Entity(tableName = "activity_logs")
data class LogMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Int,
    val hour: Int,
    val message: String,
    val category: String
)
