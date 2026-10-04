package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnimalEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ContractEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LogMessageEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.CraftingRecipe
import com.example.data.model.CropType
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.data.model.BusinessLevel
import com.example.data.model.MarketItemQuote
import com.example.data.repository.FarmRepository
import com.example.game.GameEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FarmRepository(db.farmDao())
    val gameEngine = GameEngine(db.farmDao(), viewModelScope)

    val farmState: StateFlow<FarmStateEntity?> = repository.farmState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val animals: StateFlow<List<AnimalEntity>> = repository.allAnimals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plots: StateFlow<List<CropPlotEntity>> = repository.allPlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryEntity>> = repository.inventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workshopQueue: StateFlow<List<WorkshopQueueEntity>> = repository.workshopQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shelves: StateFlow<List<ShopShelfEntity>> = repository.shelves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contracts: StateFlow<List<ContractEntity>> = repository.contracts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<LogMessageEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameSpeed: StateFlow<Float> = gameEngine.gameSpeed

    val marketQuotes: StateFlow<List<MarketItemQuote>> = repository.farmState
        .map { repository.getMarketQuotes() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userFeedback = MutableSharedFlow<String>()
    val userFeedback = _userFeedback.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.checkAndInitializeDefaults()
            gameEngine.start()
        }
    }

    fun setSpeed(speed: Float) {
        gameEngine.setSpeed(speed)
    }

    // Livestock Operations
    fun feedAnimals() {
        viewModelScope.launch {
            val success = repository.feedAllAnimals()
            _userFeedback.emit(if (success) "Animals fed with organic feed & fresh pasture!" else "Need Wheat or Coins to feed animals!")
        }
    }

    fun waterAnimals() {
        viewModelScope.launch {
            val success = repository.waterAllAnimals()
            _userFeedback.emit(if (success) "Fresh clean water distributed to all troughs!" else "Not enough clean water stored! Check rain collectors.")
        }
    }

    fun collectProduce(animalId: Long) {
        viewModelScope.launch {
            val item = repository.collectAnimalProduce(animalId)
            if (item != null) {
                _userFeedback.emit("Harvested ${item.displayName} and organic manure!")
            }
        }
    }

    fun collectAllProduce() {
        viewModelScope.launch {
            val count = repository.collectAllProduce()
            _userFeedback.emit(if (count > 0) "Gathered $count items & organic manure from livestock!" else "No products ready for collection right now.")
        }
    }

    fun buyAnimal(species: AnimalSpecies, nickname: String) {
        viewModelScope.launch {
            val success = repository.buyAnimal(species, nickname)
            _userFeedback.emit(if (success) "Welcomed ${species.displayName} to your farm!" else "Not enough Coins to buy ${species.displayName} (${species.purchaseCost} needed)!")
        }
    }

    fun petAnimal(animalId: Long) {
        viewModelScope.launch {
            repository.petAnimal(animalId)
            _userFeedback.emit("Petted animal! Happiness boosted 💕")
        }
    }

    fun sellAnimal(animalId: Long) {
        viewModelScope.launch {
            val value = repository.sellAnimal(animalId)
            if (value != null) {
                _userFeedback.emit("Successfully sold livestock for $value Coins!")
            } else {
                _userFeedback.emit("Failed to sell animal.")
            }
        }
    }

    fun processAnimalMeat(animalId: Long) {
        viewModelScope.launch {
            val result = repository.processAnimalMeat(animalId)
            if (result.isEligible) {
                val hideMsg = if (result.hideCount > 0) " & ${result.hideCount} Eco-Hide" else ""
                _userFeedback.emit("Processed into ${result.meatCount} Pasture Meat$hideMsg!")
            } else {
                _userFeedback.emit(result.rejectionReason ?: "Animal cannot be processed into meat.")
            }
        }
    }

    fun breedAnimal(animalId: Long) {
        viewModelScope.launch {
            val baby = repository.breedAnimal(animalId)
            if (baby != null) {
                _userFeedback.emit("💕 Breeding Success: Welcomed ${baby.nickname}!")
            } else {
                _userFeedback.emit("Breeding conditions not met. Need mature healthy partner.")
            }
        }
    }

    fun advanceBusinessLevel() {
        viewModelScope.launch {
            val newLevel = repository.advanceBusinessLevel()
            if (newLevel != null) {
                _userFeedback.emit("🎉 Promoted to ${newLevel.title}! ${newLevel.perkDescription}")
            } else {
                _userFeedback.emit("Requirements not met for business promotion.")
            }
        }
    }

    // Ecology & Composting Loop
    fun compostManure() {
        viewModelScope.launch {
            val success = repository.processCompostBatch()
            _userFeedback.emit(if (success) "Composted 3 Manure into 2 Rich Compost + 1 Biogas Canister!" else "Need at least 3 Raw Manure to run compost batch!")
        }
    }

    fun craftBioFertilizer() {
        viewModelScope.launch {
            val success = repository.craftBioFertilizer()
            _userFeedback.emit(if (success) "Crafted Solar Bio-Fertilizer! (+3 Eco-Score)" else "Need 2 Compost to craft Bio-Fertilizer!")
        }
    }

    // Agriculture & Crops
    fun plantCrop(plotId: Int, cropType: CropType) {
        viewModelScope.launch {
            val success = repository.plantCrop(plotId, cropType)
            _userFeedback.emit(if (success) "Planted ${cropType.displayName} in Plot #$plotId!" else "Need ${cropType.seedItem.displayName} seeds!")
        }
    }

    fun waterPlot(plotId: Int) {
        viewModelScope.launch {
            val success = repository.waterPlot(plotId)
            _userFeedback.emit(if (success) "Plot #$plotId irrigated!" else "Low water reservoir! Wait for rain or solar pump.")
        }
    }

    fun waterAllPlots() {
        viewModelScope.launch {
            val success = repository.waterAllPlots()
            _userFeedback.emit(if (success) "All crop plots irrigated!" else "Low water reservoir! Build more rain collectors.")
        }
    }

    fun fertilizePlot(plotId: Int) {
        viewModelScope.launch {
            val success = repository.fertilizePlot(plotId)
            _userFeedback.emit(if (success) "Enriched plot with organic bio-fertilizer!" else "Need Compost or Bio-Fertilizer in inventory!")
        }
    }

    fun harvestCrop(plotId: Int) {
        viewModelScope.launch {
            val success = repository.harvestCrop(plotId)
            _userFeedback.emit(if (success) "Crop harvested into inventory!" else "Crop is not ready for harvest yet.")
        }
    }

    fun harvestAllCrops() {
        viewModelScope.launch {
            val count = repository.harvestAllReadyCrops()
            _userFeedback.emit(if (count > 0) "Harvested $count crops from fields!" else "No ripe crops to harvest right now.")
        }
    }

    fun upgradeGreenhouse(plotId: Int) {
        viewModelScope.launch {
            val success = repository.upgradePlotGreenhouse(plotId)
            _userFeedback.emit(if (success) "Solar Greenhouse installed on Plot #$plotId! Weatherproof!" else "Need 220 Coins for Solar Greenhouse upgrade.")
        }
    }

    // Workshop & Processing
    fun startCrafting(recipe: CraftingRecipe) {
        viewModelScope.launch {
            val success = repository.startCrafting(recipe)
            _userFeedback.emit(if (success) "Started ${recipe.name} in ${recipe.building}!" else "Missing required ingredients for ${recipe.name}!")
        }
    }

    fun collectWorkshopTask(taskId: Long) {
        viewModelScope.launch {
            val success = repository.collectFinishedWorkshop(taskId)
            _userFeedback.emit(if (success) "Artisan goods collected to inventory!" else "Production is still in progress.")
        }
    }

    // Eco-Shop & Sales
    fun stockShelf(shelfId: Int, itemId: ItemId, qty: Int) {
        viewModelScope.launch {
            val success = repository.stockShelf(shelfId, itemId, qty)
            _userFeedback.emit(if (success) "Stocked $qty ${itemId.displayName} on Shelf #$shelfId!" else "Not enough $itemId in inventory!")
        }
    }

    fun clearShelf(shelfId: Int) {
        viewModelScope.launch {
            repository.clearShelf(shelfId)
            _userFeedback.emit("Shelf #$shelfId cleared and returned to inventory.")
        }
    }

    fun updateShelfPricing(shelfId: Int, strategy: PricingStrategy) {
        viewModelScope.launch {
            repository.updateShelfPricing(shelfId, strategy)
            _userFeedback.emit("Pricing strategy updated to ${strategy.label}!")
        }
    }

    fun wholesaleSell(itemId: ItemId, qty: Int) {
        viewModelScope.launch {
            val success = repository.sellDirectToWholesale(itemId, qty)
            _userFeedback.emit(if (success) "Sold $qty ${itemId.displayName} to settlement market!" else "Not enough in inventory.")
        }
    }

    fun fulfillContract(contractId: String) {
        viewModelScope.launch {
            val success = repository.fulfillContract(contractId)
            _userFeedback.emit(if (success) "Contract completed! Rewards received!" else "Missing requested items to deliver.")
        }
    }

    // Sustainable Fishery
    fun goFishing() {
        viewModelScope.launch {
            val success = repository.goSustainableFishing()
            _userFeedback.emit(if (success) "Sustainable catch landed! Fresh fish & seaweed added." else "River fish stock depleted! Let the ecosystem recover.")
        }
    }

    fun activateAquaponics() {
        viewModelScope.launch {
            val success = repository.activateAquaponics()
            _userFeedback.emit(if (success) "Solar Aquaponics installed! Producing sustainable fish & mint!" else "Need 480 Coins to build Solar Aquaponics system.")
        }
    }

    // Energy & Infrastructure
    fun buySolarPanel() {
        viewModelScope.launch {
            val success = repository.buySolarPanel()
            _userFeedback.emit(if (success) "New Solar Panel connected to microgrid!" else "Not enough Coins for Solar Panel.")
        }
    }

    fun buyWindTurbine() {
        viewModelScope.launch {
            val success = repository.buyWindTurbine()
            _userFeedback.emit(if (success) "Micro Wind Turbine erected!" else "Not enough Coins for Wind Turbine.")
        }
    }

    fun buyRainCollector() {
        viewModelScope.launch {
            val success = repository.buyRainCollector()
            _userFeedback.emit(if (success) "Rainwater Silo built! Water capacity increased." else "Not enough Coins for Rain Collector.")
        }
    }

    fun buySeeds(seedItem: ItemId, quantity: Int) {
        viewModelScope.launch {
            val success = repository.buySeeds(seedItem, quantity)
            _userFeedback.emit(if (success) "Purchased $quantity ${seedItem.displayName}!" else "Not enough Coins to buy seeds.")
        }
    }

    fun upgradeSettlementTier() {
        viewModelScope.launch {
            val success = repository.upgradeSettlementTier()
            _userFeedback.emit(if (success) "🎉 Settlement Upgraded to next Solarpunk Tier!" else "Requirements not met! Check required Coins & Eco-Score.")
        }
    }
}
