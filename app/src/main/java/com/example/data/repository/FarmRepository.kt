package com.example.data.repository

import com.example.data.local.AnimalEntity
import com.example.data.local.ContractEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmDao
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LogMessageEntity
import com.example.data.local.MarketQuoteEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.BusinessLevel
import com.example.data.model.CraftingRecipe
import com.example.data.model.CropType
import com.example.data.model.ItemCategory
import com.example.data.model.ItemId
import com.example.data.model.LivestockValuation
import com.example.data.model.MarketDemand
import com.example.data.model.MarketItemQuote
import com.example.data.model.MeatProcessingYield
import com.example.data.model.PricingStrategy
import com.example.data.model.Season
import com.example.data.model.SettlementTier
import com.example.data.model.WeatherType
import com.example.data.model.WorkshopRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class FarmRepository(private val dao: FarmDao) {

    private val stateMutex = Mutex()

    val farmState: Flow<FarmStateEntity?> = dao.getFarmState()
    val allAnimals: Flow<List<AnimalEntity>> = dao.getAllAnimals()
    val allPlots: Flow<List<CropPlotEntity>> = dao.getAllPlots()
    val inventory: Flow<List<InventoryEntity>> = dao.getInventory()
    val workshopQueue: Flow<List<WorkshopQueueEntity>> = dao.getWorkshopQueue()
    val shelves: Flow<List<ShopShelfEntity>> = dao.getAllShelves()
    val contracts: Flow<List<ContractEntity>> = dao.getAllContracts()
    val allMarketQuotes: Flow<List<MarketQuoteEntity>> = dao.getAllMarketQuotes()
    val recentLogs: Flow<List<LogMessageEntity>> = dao.getRecentLogs()

    companion object {
        val MONITORED_MARKET_ITEMS = listOf(
            ItemId.EGGS,
            ItemId.DUCK_EGGS,
            ItemId.COW_MILK,
            ItemId.GOAT_MILK,
            ItemId.SHEEP_WOOL,
            ItemId.HONEY,
            ItemId.PREMIUM_HONEY,
            ItemId.MEAT,
            ItemId.PACKAGED_MEAT,
            ItemId.LEATHER,
            ItemId.WHEAT,
            ItemId.CORN,
            ItemId.TOMATO,
            ItemId.CARROT,
            ItemId.STRAWBERRY,
            ItemId.MINT,
            ItemId.LAVENDER,
            ItemId.FLOUR,
            ItemId.ARTISAN_BREAD,
            ItemId.ARTISAN_CHEESE,
            ItemId.ORGANIC_BUTTER,
            ItemId.SOLAR_JUICE,
            ItemId.BERRY_JAM,
            ItemId.ECO_FABRIC,
            ItemId.HANDMADE_BLANKET,
            ItemId.LEATHER_TOOLBELT,
            ItemId.HERBAL_BALM,
            ItemId.TILAPIA,
            ItemId.RIVER_TROUT,
            ItemId.SMOKED_FISH
        )
    }

    suspend fun savePlayerPosition(x: Float, y: Float, z: Float, yaw: Float) {
        dao.updatePlayerPosition(x, y, z, yaw)
    }

    suspend fun savePlayerAndCameraState(x: Float, y: Float, z: Float, yaw: Float, camYaw: Float, camPitch: Float, camDist: Float) {
        dao.updatePlayerAndCameraState(x, y, z, yaw, camYaw, camPitch, camDist)
    }

    suspend fun saveAnimalPosition(animalId: Long, x: Float, y: Float, z: Float, yaw: Float) {
        dao.updateAnimalPosition(animalId, x, y, z, yaw)
    }

    suspend fun checkAndInitializeDefaults() = stateMutex.withLock {
        val existingState = dao.getFarmStateDirect()
        if (existingState == null) {
            // First time launch: Initialize starter Solarpunk Farmstead
            dao.insertOrUpdateFarmState(
                FarmStateEntity(
                    coins = 450,
                    ecoHarmonyScore = 52,
                    day = 1,
                    hour = 8,
                    season = Season.SPRING,
                    weather = WeatherType.SUNNY,
                    weatherHoursRemaining = 8,
                    solarEnergy = 20f,
                    batteryStored = 35f,
                    batteryMax = 50f,
                    waterStored = 70f,
                    waterMax = 100f,
                    compostBinsActive = 1,
                    compostProgress = 0.2f,
                    settlementTier = SettlementTier.HOMESTEAD,
                    solarPanelsCount = 2,
                    windTurbinesCount = 1,
                    rainCollectorsCount = 2
                )
            )

            // Starter Livestock: mature animals with appropriate starting age
            dao.insertAnimal(AnimalEntity(species = AnimalSpecies.CHICKEN, nickname = "Pippa", hunger = 0.1f, thirst = 0.1f, health = 1f, happiness = 0.9f, ageDays = AnimalSpecies.CHICKEN.startingPurchaseAgeDays))
            dao.insertAnimal(AnimalEntity(species = AnimalSpecies.CHICKEN, nickname = "Sunny", hunger = 0.2f, thirst = 0.1f, health = 1f, happiness = 0.85f, ageDays = AnimalSpecies.CHICKEN.startingPurchaseAgeDays))
            dao.insertAnimal(AnimalEntity(species = AnimalSpecies.COW, nickname = "Bessie", hunger = 0.2f, thirst = 0.15f, health = 1f, happiness = 0.9f, ageDays = AnimalSpecies.COW.startingPurchaseAgeDays))
            dao.insertAnimal(AnimalEntity(species = AnimalSpecies.GOAT, nickname = "Barnaby", hunger = 0.1f, thirst = 0.2f, health = 1f, happiness = 0.95f, ageDays = AnimalSpecies.GOAT.startingPurchaseAgeDays))
            dao.insertAnimal(AnimalEntity(species = AnimalSpecies.BEES, nickname = "Solar Swarm #1", hunger = 0.05f, thirst = 0.05f, health = 1f, happiness = 1f, ageDays = AnimalSpecies.BEES.startingPurchaseAgeDays))

            // Starter 8 Crop Plots
            val initialPlots = (1..8).map { id ->
                when (id) {
                    1 -> CropPlotEntity(id = id, cropType = CropType.WHEAT, growthProgress = 0.6f, waterLevel = 0.8f, soilFertility = 1.0f)
                    2 -> CropPlotEntity(id = id, cropType = CropType.TOMATO, growthProgress = 0.3f, waterLevel = 0.9f, soilFertility = 1.1f)
                    3 -> CropPlotEntity(id = id, cropType = CropType.CARROT, growthProgress = 0.8f, waterLevel = 0.7f, soilFertility = 1.0f)
                    4 -> CropPlotEntity(id = id, cropType = CropType.STRAWBERRY, growthProgress = 0.2f, waterLevel = 0.8f, soilFertility = 1.2f)
                    5 -> CropPlotEntity(id = id, cropType = CropType.HERBS, growthProgress = 0.9f, waterLevel = 0.9f, soilFertility = 1.1f)
                    else -> CropPlotEntity(id = id, cropType = null, growthProgress = 0f, waterLevel = 0.5f, soilFertility = 1.0f)
                }
            }
            dao.insertPlots(initialPlots)

            // Starter Inventory
            dao.setInventoryItem(InventoryEntity(ItemId.SEED_WHEAT, 4))
            dao.setInventoryItem(InventoryEntity(ItemId.SEED_CORN, 3))
            dao.setInventoryItem(InventoryEntity(ItemId.SEED_TOMATO, 3))
            dao.setInventoryItem(InventoryEntity(ItemId.SEED_CARROT, 3))
            dao.setInventoryItem(InventoryEntity(ItemId.SEED_HERB, 2))
            dao.setInventoryItem(InventoryEntity(ItemId.EGGS, 6))
            dao.setInventoryItem(InventoryEntity(ItemId.COW_MILK, 3))
            dao.setInventoryItem(InventoryEntity(ItemId.MANURE, 8))
            dao.setInventoryItem(InventoryEntity(ItemId.COMPOST, 4))
            dao.setInventoryItem(InventoryEntity(ItemId.BIO_FERTILIZER, 2))
            dao.setInventoryItem(InventoryEntity(ItemId.WHEAT, 6))
            dao.setInventoryItem(InventoryEntity(ItemId.HONEY, 3))

            // Starter Retail Shop Shelves (6 display shelves)
            val initialShelves = listOf(
                ShopShelfEntity(shelfId = 1, stockedItemId = ItemId.EGGS, quantity = 4, pricingStrategy = PricingStrategy.FAIR_TRADE),
                ShopShelfEntity(shelfId = 2, stockedItemId = ItemId.COW_MILK, quantity = 2, pricingStrategy = PricingStrategy.FAIR_TRADE),
                ShopShelfEntity(shelfId = 3, stockedItemId = ItemId.HONEY, quantity = 2, pricingStrategy = PricingStrategy.PREMIUM_ORGANIC),
                ShopShelfEntity(shelfId = 4, stockedItemId = null, quantity = 0, pricingStrategy = PricingStrategy.FAIR_TRADE),
                ShopShelfEntity(shelfId = 5, stockedItemId = null, quantity = 0, pricingStrategy = PricingStrategy.FAIR_TRADE),
                ShopShelfEntity(shelfId = 6, stockedItemId = null, quantity = 0, pricingStrategy = PricingStrategy.FAIR_TRADE)
            )
            dao.insertShelves(initialShelves)

            // Starter NPC Settlement Contracts
            val initialContracts = listOf(
                ContractEntity(
                    id = "c1",
                    clientName = "Chef Lin",
                    clientAvatarEmoji = "👨‍🍳",
                    clientRole = "Solar Bistro Owner",
                    requestedItem = ItemId.EGGS,
                    requestedQuantity = 8,
                    rewardCoins = 90,
                    rewardEcoScore = 4,
                    expiryDay = 4
                ),
                ContractEntity(
                    id = "c2",
                    clientName = "Healer Maya",
                    clientAvatarEmoji = "🌿",
                    clientRole = "Settlement Apothecary",
                    requestedItem = ItemId.HONEY,
                    requestedQuantity = 3,
                    rewardCoins = 105,
                    rewardEcoScore = 6,
                    expiryDay = 5
                ),
                ContractEntity(
                    id = "c3",
                    clientName = "Master Tomas",
                    clientAvatarEmoji = "🪵",
                    clientRole = "Eco-Architect",
                    requestedItem = ItemId.BIO_FERTILIZER,
                    requestedQuantity = 2,
                    rewardCoins = 85,
                    rewardEcoScore = 5,
                    expiryDay = 6
                )
            )
            dao.insertContracts(initialContracts)

            addLog("Welcome to your Solarpunk Farm! Your homestead is powered by clean sun & wind.", "SYSTEM")
        }

        // Ensure market quotes are seeded
        if (dao.getAllMarketQuotesDirect().isEmpty()) {
            val state = dao.getFarmStateDirect()
            recalculateDailyMarketQuotesInternal(state?.day ?: 1)
        }
    }

    private suspend fun addLog(message: String, category: String) {
        val state = dao.getFarmStateDirect()
        val day = state?.day ?: 1
        val hour = state?.hour ?: 8
        dao.insertLog(LogMessageEntity(day = day, hour = hour, message = message, category = category))
    }

    // --- LIVESTOCK CARE & PRODUCTS ---

    suspend fun feedAllAnimals(): Boolean = stateMutex.withLock {
        val animals = dao.getAllAnimalsDirect()
        if (animals.isEmpty()) return false

        val wheatStock = dao.getInventoryItem(ItemId.WHEAT)?.quantity ?: 0
        val state = dao.getFarmStateDirect() ?: return false

        if (wheatStock >= 1) {
            dao.addInventoryQuantity(ItemId.WHEAT, -1)
        } else if (state.coins >= 10) {
            dao.insertOrUpdateFarmState(
                state.copy(
                    coins = state.coins - 10,
                    totalExpenses = state.totalExpenses + 10
                )
            )
        } else {
            return false
        }

        val updated = animals.map {
            it.copy(
                hunger = (it.hunger - 0.7f).coerceAtLeast(0f),
                happiness = (it.happiness + 0.15f).coerceAtMost(1f)
            )
        }
        dao.updateAnimals(updated)
        addLog("Fed all livestock with organic feed & fresh pasture.", "ANIMALS")
        return true
    }

    suspend fun waterAllAnimals(): Boolean = stateMutex.withLock {
        val animals = dao.getAllAnimalsDirect()
        val state = dao.getFarmStateDirect() ?: return false
        val waterNeeded = animals.size * 1.5f
        if (state.waterStored < waterNeeded) return false

        dao.insertOrUpdateFarmState(state.copy(waterStored = state.waterStored - waterNeeded))
        val updated = animals.map {
            it.copy(
                thirst = (it.thirst - 0.6f).coerceAtLeast(0f),
                health = (it.health + 0.05f).coerceAtMost(1f)
            )
        }
        dao.updateAnimals(updated)
        addLog("Replenished solar troughs with clean spring water.", "ANIMALS")
        return true
    }

    suspend fun collectAnimalProduce(animalId: Long): ItemId? = stateMutex.withLock {
        val animals = dao.getAllAnimalsDirect()
        val target = animals.find { it.id == animalId } ?: return null
        if (!target.produceReady) return null

        // Species without recurring produce (like Pig) cannot produce recurring goods
        val produceItem = target.species.primaryProduce ?: return null
        var yield = 1
        if (target.happiness > 0.8f) yield += 1

        dao.addInventoryQuantity(produceItem, yield)

        // Animals also produce manure for the zero-waste loop
        if (target.species.producesManure) {
            val manureAmount = if (target.species == AnimalSpecies.COW) 3 else 1
            dao.addInventoryQuantity(ItemId.MANURE, manureAmount)
            addLog("Harvested $yield ${produceItem.displayName} and $manureAmount Manure from ${target.nickname}!", "ANIMALS")
        } else {
            addLog("Harvested $yield ${produceItem.displayName} from ${target.nickname}!", "ANIMALS")
        }

        dao.updateAnimal(
            target.copy(
                produceReady = false,
                hoursUntilProduce = target.species.produceFrequencyHours
            )
        )
        return produceItem
    }

    suspend fun collectAllProduce(): Int = stateMutex.withLock {
        val animals = dao.getAllAnimalsDirect()
        var collectedCount = 0
        var totalManure = 0

        animals.filter { it.produceReady && it.species.primaryProduce != null }.forEach { animal ->
            val produceItem = animal.species.primaryProduce ?: return@forEach
            val yield = if (animal.happiness > 0.8f) 2 else 1
            dao.addInventoryQuantity(produceItem, yield)
            if (animal.species.producesManure) {
                totalManure += if (animal.species == AnimalSpecies.COW) 3 else 1
            }
            dao.updateAnimal(
                animal.copy(
                    produceReady = false,
                    hoursUntilProduce = animal.species.produceFrequencyHours
                )
            )
            collectedCount += yield
        }

        if (totalManure > 0) {
            dao.addInventoryQuantity(ItemId.MANURE, totalManure)
        }
        if (collectedCount > 0) {
            addLog("Gathered all ready livestock produce ($collectedCount items, +$totalManure Manure).", "ANIMALS")
        }
        return collectedCount
    }

    suspend fun buyAnimal(species: AnimalSpecies, nickname: String): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        if (state.coins < species.purchaseCost) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - species.purchaseCost,
                totalExpenses = state.totalExpenses + species.purchaseCost
            )
        )
        dao.insertAnimal(
            AnimalEntity(
                species = species,
                nickname = nickname.ifBlank { "${species.displayName} #${Random.nextInt(100, 999)}" },
                hunger = 0.1f,
                thirst = 0.1f,
                health = 1f,
                happiness = 0.95f,
                ageDays = species.startingPurchaseAgeDays,
                isPregnant = false,
                pregnancyHours = 0
            )
        )
        addLog("Welcomed new ${species.displayName} '$nickname' to your sanctuary.", "ANIMALS")
        return true
    }

    suspend fun petAnimal(animalId: Long) = stateMutex.withLock {
        val animal = dao.getAllAnimalsDirect().find { it.id == animalId } ?: return
        dao.updateAnimal(
            animal.copy(happiness = (animal.happiness + 0.15f).coerceAtMost(1f))
        )
        addLog("Petted ${animal.nickname} - they are glowing with affection! 💕", "ANIMALS")
    }

    // --- LIVE ANIMAL SELLING ---
    suspend fun sellAnimal(animalId: Long): Int? = stateMutex.withLock {
        val animal = dao.getAllAnimalsDirect().find { it.id == animalId } ?: return null
        val state = dao.getFarmStateDirect() ?: return null

        val saleValue = LivestockValuation.calculateSaleValue(
            animal.species,
            animal.ageDays,
            animal.health,
            animal.happiness
        )

        dao.deleteAnimal(animal)
        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins + saleValue,
                totalEarnings = state.totalEarnings + saleValue,
                salesToday = state.salesToday + saleValue,
                livestockSoldTotal = state.livestockSoldTotal + 1,
                businessReputation = (state.businessReputation + 1).coerceAtMost(100)
            )
        )
        addLog("Sold ${animal.species.displayName} '${animal.nickname}' for $saleValue Coins to livestock market.", "ANIMALS")
        return saleValue
    }

    // --- MEAT & HIDE PROCESSING ---
    suspend fun processAnimalMeat(animalId: Long): MeatProcessingYield = stateMutex.withLock {
        val animal = dao.getAllAnimalsDirect().find { it.id == animalId }
            ?: return MeatProcessingYield(0, 0, false, "Animal not found.")

        val yield = LivestockValuation.calculateMeatYield(
            animal.species,
            animal.nickname,
            animal.ageDays,
            animal.health
        )

        if (!yield.isEligible) {
            return yield
        }

        val state = dao.getFarmStateDirect()
        dao.addInventoryQuantity(ItemId.MEAT, yield.meatCount)
        if (yield.hideCount > 0) {
            dao.addInventoryQuantity(ItemId.LEATHER, yield.hideCount)
        }

        dao.deleteAnimal(animal)

        if (state != null) {
            dao.insertOrUpdateFarmState(
                state.copy(
                    meatProcessedTotal = state.meatProcessedTotal + 1
                )
            )
        }

        val hideText = if (yield.hideCount > 0) " and ${yield.hideCount} Eco-Hide" else ""
        addLog("Processed ${animal.species.displayName} '${animal.nickname}' into ${yield.meatCount} Pasture Meat$hideText.", "ANIMALS")
        return yield
    }

    // --- ANIMAL BREEDING & PREGNANCY ---
    suspend fun breedAnimal(animalId: Long): AnimalEntity? = stateMutex.withLock {
        val animal = dao.getAllAnimalsDirect().find { it.id == animalId } ?: return null
        if (animal.species == AnimalSpecies.BEES) {
            addLog("Bees reproduce naturally via swarm division when flowers are abundant.", "ANIMALS")
            return null
        }
        if (animal.isPregnant) {
            addLog("${animal.nickname} is already pregnant (${animal.pregnancyHours}/${animal.species.gestationHours}h).", "ANIMALS")
            return null
        }
        if (animal.ageDays < animal.species.breedingMaturityDays) {
            addLog("${animal.nickname} is still a juvenile (${animal.ageDays}/${animal.species.breedingMaturityDays}d). Cannot breed yet.", "ANIMALS")
            return null
        }
        if (animal.health < 0.6f || animal.hunger > 0.5f || animal.thirst > 0.5f) {
            addLog("${animal.nickname} needs better health and feed before breeding.", "ANIMALS")
            return null
        }

        val allAnimals = dao.getAllAnimalsDirect()
        val partner = allAnimals.find {
            it.id != animal.id &&
            it.species == animal.species &&
            it.ageDays >= it.species.breedingMaturityDays &&
            it.health >= 0.6f &&
            !it.isPregnant
        }

        if (partner == null) {
            addLog("No eligible mature mate found for ${animal.nickname}. Need another healthy adult ${animal.species.displayName}.", "ANIMALS")
            return null
        }

        // Begin Pregnancy
        val updatedMother = animal.copy(isPregnant = true, pregnancyHours = 0)
        dao.updateAnimal(updatedMother)
        dao.updateAnimal(partner.copy(happiness = (partner.happiness + 0.1f).coerceAtMost(1f)))

        val state = dao.getFarmStateDirect()
        if (state != null) {
            dao.insertOrUpdateFarmState(
                state.copy(ecoHarmonyScore = (state.ecoHarmonyScore + 1).coerceAtMost(100))
            )
        }

        addLog("💕 Breeding Success: ${animal.nickname} is now pregnant! (Gestation: ${animal.species.gestationHours}h)", "ANIMALS")
        return updatedMother
    }

    // --- COMPOST & ZERO WASTE LOOP ---
    suspend fun processCompostBatch(): Boolean = stateMutex.withLock {
        val manure = dao.getInventoryItem(ItemId.MANURE)?.quantity ?: 0
        if (manure < 3) return false

        dao.addInventoryQuantity(ItemId.MANURE, -3)
        dao.addInventoryQuantity(ItemId.COMPOST, 2)
        dao.addInventoryQuantity(ItemId.BIOGAS_CANISTER, 1)

        val state = dao.getFarmStateDirect()
        if (state != null) {
            val newScore = (state.ecoHarmonyScore + 2).coerceAtMost(100)
            dao.insertOrUpdateFarmState(state.copy(ecoHarmonyScore = newScore))
        }

        addLog("Zero-Waste Loop: 3 Manure composted into 2 Organic Compost + 1 Biogas Canister! (+2 Eco-Score)", "ECOLOGY")
        return true
    }

    suspend fun craftBioFertilizer(): Boolean = stateMutex.withLock {
        val compost = dao.getInventoryItem(ItemId.COMPOST)?.quantity ?: 0
        if (compost < 2) return false

        val state = dao.getFarmStateDirect() ?: return false
        val availableEnergy = state.solarEnergy + state.batteryStored
        if (availableEnergy < 1f) return false

        dao.addInventoryQuantity(ItemId.COMPOST, -2)
        dao.addInventoryQuantity(ItemId.BIO_FERTILIZER, 1)

        val newBattery = (state.batteryStored - 1f).coerceAtLeast(0f)
        dao.insertOrUpdateFarmState(
            state.copy(
                batteryStored = newBattery,
                ecoHarmonyScore = (state.ecoHarmonyScore + 2).coerceAtMost(100)
            )
        )

        addLog("Crafted Solar Bio-Fertilizer from rich compost. Great for doubling crop yield!", "ECOLOGY")
        return true
    }

    // --- AGRICULTURE SYSTEM ---
    suspend fun plantCrop(plotId: Int, cropType: CropType): Boolean = stateMutex.withLock {
        val seedStock = dao.getInventoryItem(cropType.seedItem)?.quantity ?: 0
        if (seedStock < 1) return false

        val plot = dao.getAllPlotsDirect().find { it.id == plotId } ?: return false
        if (plot.cropType != null) return false

        dao.addInventoryQuantity(cropType.seedItem, -1)
        dao.updatePlot(
            plot.copy(
                cropType = cropType,
                growthProgress = 0f,
                waterLevel = 0.8f,
                isReadyForHarvest = false
            )
        )
        addLog("Planted ${cropType.displayName} on Plot #$plotId.", "CROPS")
        return true
    }

    suspend fun waterPlot(plotId: Int): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        if (state.waterStored < 5f) return false

        val plot = dao.getAllPlotsDirect().find { it.id == plotId } ?: return false
        dao.insertOrUpdateFarmState(state.copy(waterStored = state.waterStored - 5f))
        dao.updatePlot(plot.copy(waterLevel = 1.0f))
        addLog("Irrigated Plot #$plotId with clean rainwater.", "CROPS")
        return true
    }

    suspend fun waterAllPlots(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        val plots = dao.getAllPlotsDirect().filter { it.cropType != null && it.waterLevel < 0.6f }
        if (plots.isEmpty()) return true

        val waterNeeded = plots.size * 4f
        if (state.waterStored < waterNeeded) return false

        dao.insertOrUpdateFarmState(state.copy(waterStored = state.waterStored - waterNeeded))
        dao.updatePlots(plots.map { it.copy(waterLevel = 1.0f) })
        addLog("Irrigated ${plots.size} thirsty crop plots.", "CROPS")
        return true
    }

    suspend fun fertilizePlot(plotId: Int): Boolean = stateMutex.withLock {
        val fert = dao.getInventoryItem(ItemId.BIO_FERTILIZER)?.quantity ?: 0
        val comp = dao.getInventoryItem(ItemId.COMPOST)?.quantity ?: 0
        if (fert <= 0 && comp <= 0) return false

        val plot = dao.getAllPlotsDirect().find { it.id == plotId } ?: return false

        val boost = if (fert > 0) {
            dao.addInventoryQuantity(ItemId.BIO_FERTILIZER, -1)
            0.4f
        } else {
            dao.addInventoryQuantity(ItemId.COMPOST, -1)
            0.2f
        }

        val newFertility = (plot.soilFertility + boost).coerceAtMost(1.8f)
        dao.updatePlot(plot.copy(soilFertility = newFertility))
        addLog("Applied organic fertilizer to Plot #$plotId (Fertility now ${(newFertility * 100).toInt()}%).", "ECOLOGY")
        return true
    }

    suspend fun harvestCrop(plotId: Int): Boolean = stateMutex.withLock {
        val plot = dao.getAllPlotsDirect().find { it.id == plotId } ?: return false
        val crop = plot.cropType ?: return false
        if (!plot.isReadyForHarvest && plot.growthProgress < 1.0f) return false

        val state = dao.getFarmStateDirect()
        val seasonMultiplier = if (state?.season == crop.preferredSeason) 1.25f else 1.0f
        val yieldCount = ((crop.baseYield * plot.soilFertility * seasonMultiplier)).toInt().coerceAtLeast(1)

        dao.addInventoryQuantity(crop.harvestItem, yieldCount)

        // Seed saving
        if (Random.nextFloat() < 0.6f) {
            dao.addInventoryQuantity(crop.seedItem, 1)
        }

        dao.updatePlot(
            plot.copy(
                cropType = null,
                growthProgress = 0f,
                isReadyForHarvest = false
            )
        )
        addLog("Harvested $yieldCount ${crop.displayName} from Plot #$plotId!", "CROPS")
        return true
    }

    suspend fun harvestAllReadyCrops(): Int = stateMutex.withLock {
        val plots = dao.getAllPlotsDirect().filter { it.cropType != null && (it.isReadyForHarvest || it.growthProgress >= 1f) }
        var total = 0
        plots.forEach { plot ->
            val crop = plot.cropType ?: return@forEach
            val yield = (crop.baseYield * plot.soilFertility).toInt().coerceAtLeast(1)
            dao.addInventoryQuantity(crop.harvestItem, yield)
            if (Random.nextFloat() < 0.5f) dao.addInventoryQuantity(crop.seedItem, 1)
            dao.updatePlot(plot.copy(cropType = null, growthProgress = 0f, isReadyForHarvest = false))
            total += yield
        }
        if (total > 0) addLog("Harvested all ripe fields: $total crops added to barn.", "CROPS")
        return total
    }

    suspend fun upgradePlotGreenhouse(plotId: Int): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        val cost = 220
        if (state.coins < cost) return false

        val plot = dao.getAllPlotsDirect().find { it.id == plotId } ?: return false
        if (plot.hasGreenhouse) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - cost,
                totalExpenses = state.totalExpenses + cost,
                ecoHarmonyScore = (state.ecoHarmonyScore + 4).coerceAtMost(100)
            )
        )
        dao.updatePlot(plot.copy(hasGreenhouse = true))
        addLog("Installed Solar Glass Greenhouse on Plot #$plotId. Weatherproof year-round!", "SETTLEMENT")
        return true
    }

    // --- WORKSHOP / PROCESSING ---
    suspend fun startCrafting(recipe: CraftingRecipe): Boolean = stateMutex.withLock {
        if (recipe.inputQuantity <= 0) return false
        val state = dao.getFarmStateDirect() ?: return false

        val primaryStock = dao.getInventoryItem(recipe.inputItem)?.quantity ?: 0
        if (primaryStock < recipe.inputQuantity) return false

        if (recipe.secondaryInput != null && recipe.secondaryQuantity > 0) {
            val secStock = dao.getInventoryItem(recipe.secondaryInput)?.quantity ?: 0
            if (secStock < recipe.secondaryQuantity) return false
        }

        // Energy requirement verification
        val totalAvailableBattery = state.batteryStored
        if (totalAvailableBattery < recipe.energyCost) {
            addLog("Insufficient battery energy (${state.batteryStored.toInt()}/${recipe.energyCost.toInt()} kWh needed) for ${recipe.name}!", "WORKSHOP")
            return false
        }

        // Deduct ingredients atomically
        dao.addInventoryQuantity(recipe.inputItem, -recipe.inputQuantity)
        if (recipe.secondaryInput != null && recipe.secondaryQuantity > 0) {
            dao.addInventoryQuantity(recipe.secondaryInput, -recipe.secondaryQuantity)
        }

        // Deduct energy
        val newBattery = (state.batteryStored - recipe.energyCost).coerceAtLeast(0f)
        dao.insertOrUpdateFarmState(state.copy(batteryStored = newBattery))

        // Queue in workshop
        dao.insertWorkshopTask(
            WorkshopQueueEntity(
                recipeId = recipe.id,
                progressHours = 0f,
                totalHours = recipe.durationHours,
                isFinished = false
            )
        )
        addLog("Started production: ${recipe.name} in ${recipe.building} (-${recipe.energyCost.toInt()} kWh energy).", "WORKSHOP")
        return true
    }

    suspend fun collectFinishedWorkshop(taskId: Long): Boolean = stateMutex.withLock {
        val task = dao.getWorkshopQueueDirect().find { it.id == taskId } ?: return false
        if (!task.isFinished) return false

        val recipe = WorkshopRecipes.ALL.find { it.id == task.recipeId } ?: return false
        dao.addInventoryQuantity(recipe.outputItem, recipe.outputQuantity)
        dao.deleteWorkshopTask(task)
        addLog("Collected ${recipe.outputQuantity} ${recipe.outputItem.displayName} from ${recipe.building}!", "WORKSHOP")
        return true
    }

    // --- RETAIL ECO-SHOP & SALES ---
    suspend fun stockShelf(shelfId: Int, itemId: ItemId, quantityToAdd: Int): Boolean = stateMutex.withLock {
        if (quantityToAdd <= 0) return false
        val stock = dao.getInventoryItem(itemId)?.quantity ?: 0
        if (stock < quantityToAdd) return false

        val shelf = dao.getAllShelvesDirect().find { it.shelfId == shelfId } ?: return false
        dao.addInventoryQuantity(itemId, -quantityToAdd)

        val updatedShelf = if (shelf.stockedItemId == itemId) {
            shelf.copy(quantity = shelf.quantity + quantityToAdd)
        } else {
            // Return old item if any
            if (shelf.stockedItemId != null && shelf.quantity > 0) {
                dao.addInventoryQuantity(shelf.stockedItemId, shelf.quantity)
            }
            shelf.copy(stockedItemId = itemId, quantity = quantityToAdd)
        }
        dao.updateShelf(updatedShelf)
        addLog("Stocked $quantityToAdd ${itemId.displayName} on Shelf #$shelfId.", "SHOP")
        return true
    }

    suspend fun clearShelf(shelfId: Int): Boolean = stateMutex.withLock {
        val shelf = dao.getAllShelvesDirect().find { it.shelfId == shelfId } ?: return false
        if (shelf.stockedItemId != null && shelf.quantity > 0) {
            dao.addInventoryQuantity(shelf.stockedItemId, shelf.quantity)
        }
        dao.updateShelf(shelf.copy(stockedItemId = null, quantity = 0))
        return true
    }

    suspend fun updateShelfPricing(shelfId: Int, strategy: PricingStrategy) = stateMutex.withLock {
        val shelf = dao.getAllShelvesDirect().find { it.shelfId == shelfId } ?: return
        dao.updateShelf(shelf.copy(pricingStrategy = strategy))
        addLog("Set Shelf #$shelfId price strategy to ${strategy.label}.", "SHOP")
    }

    // --- WHOLESALE & AUTHORITATIVE PRICING ---

    suspend fun getAuthoritativeMarketPrice(itemId: ItemId): Int {
        val existing = dao.getMarketQuoteDirect(itemId)
        if (existing != null) return existing.currentPrice

        val state = dao.getFarmStateDirect()
        val quotes = recalculateDailyMarketQuotesInternal(state?.day ?: 1)
        return quotes.find { it.itemId == itemId }?.currentPrice ?: itemId.basePrice
    }

    suspend fun sellDirectToWholesale(itemId: ItemId, quantity: Int): Boolean = stateMutex.withLock {
        if (quantity <= 0) return false
        val stock = dao.getInventoryItem(itemId)?.quantity ?: 0
        if (stock < quantity) return false

        val state = dao.getFarmStateDirect() ?: return false
        val unitPrice = getAuthoritativeMarketPrice(itemId)
        val wholesaleBonus = if (state.businessLevel.level >= BusinessLevel.LEVEL_6.level) (unitPrice * 0.1f).toInt() else 0
        val finalUnitPrice = unitPrice + wholesaleBonus
        val totalRevenue = finalUnitPrice * quantity

        dao.addInventoryQuantity(itemId, -quantity)
        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins + totalRevenue,
                totalEarnings = state.totalEarnings + totalRevenue,
                salesToday = state.salesToday + totalRevenue,
                wholesaleIncomeTotal = state.wholesaleIncomeTotal + totalRevenue
            )
        )
        addLog("Wholesale Market: Sold $quantity ${itemId.displayName} for $totalRevenue Coins (${finalUnitPrice}c/ea).", "MARKET")
        return true
    }

    suspend fun fulfillContract(contractId: String): Boolean = stateMutex.withLock {
        val contract = dao.getAllContractsDirect().find { it.id == contractId } ?: return false
        if (contract.isCompleted || contract.isPenalized) return false
        if (contract.requestedQuantity <= 0) return false

        val stock = dao.getInventoryItem(contract.requestedItem)?.quantity ?: 0
        if (stock < contract.requestedQuantity) return false

        val state = dao.getFarmStateDirect() ?: return false

        dao.addInventoryQuantity(contract.requestedItem, -contract.requestedQuantity)
        dao.updateContract(contract.copy(isCompleted = true))

        val wholesaleBonus = if (state.businessLevel.level >= BusinessLevel.LEVEL_6.level) (contract.rewardCoins * 0.2f).toInt() else 0
        val finalReward = contract.rewardCoins + wholesaleBonus

        val newCoins = state.coins + finalReward
        val newEco = (state.ecoHarmonyScore + contract.rewardEcoScore).coerceAtMost(100)
        val newReputation = (state.businessReputation + contract.rewardEcoScore * 2).coerceAtMost(100)
        dao.insertOrUpdateFarmState(
            state.copy(
                coins = newCoins,
                ecoHarmonyScore = newEco,
                totalEarnings = state.totalEarnings + finalReward,
                salesToday = state.salesToday + finalReward,
                wholesaleIncomeTotal = state.wholesaleIncomeTotal + finalReward,
                businessReputation = newReputation
            )
        )

        addLog("Fulfilled wholesale contract for ${contract.clientName}! Earned +$finalReward Coins & +${contract.rewardEcoScore} Eco-Score.", "SHOP")
        return true
    }

    // --- SUSTAINABLE FISHING & AQUAPONICS ---
    suspend fun goSustainableFishing(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        if (state.fishPopulationHealth < 0.3f) {
            addLog("River fish stock is depleted! Wait for ecosystem recovery before fishing.", "FISHERY")
            return false
        }

        val fishCaught = if (Random.nextFloat() > 0.4f) ItemId.TILAPIA else ItemId.RIVER_TROUT
        dao.addInventoryQuantity(fishCaught, 2)
        dao.addInventoryQuantity(ItemId.SEAWEED, 1)

        val newHealth = (state.fishPopulationHealth - 0.04f).coerceAtLeast(0.1f)
        dao.insertOrUpdateFarmState(state.copy(fishPopulationHealth = newHealth))
        addLog("Sustainable Fishing: Caught 2 ${fishCaught.displayName} & 1 Seaweed. River health: ${(newHealth * 100).toInt()}%", "FISHERY")
        return true
    }

    suspend fun activateAquaponics(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        if (state.coins < 480) return false
        if (state.aquaponicsActive) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - 480,
                totalExpenses = state.totalExpenses + 480,
                aquaponicsActive = true,
                ecoHarmonyScore = (state.ecoHarmonyScore + 8).coerceAtMost(100)
            )
        )
        addLog("Installed Solar Closed-Loop Aquaponics System! Clean fish farming with zero river impact.", "FISHERY")
        return true
    }

    // --- INFRASTRUCTURE & SETTLEMENT ---
    suspend fun buySolarPanel(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        val cost = 160 + (state.solarPanelsCount * 40)
        if (state.coins < cost) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - cost,
                totalExpenses = state.totalExpenses + cost,
                solarPanelsCount = state.solarPanelsCount + 1,
                batteryMax = state.batteryMax + 20f,
                ecoHarmonyScore = (state.ecoHarmonyScore + 4).coerceAtMost(100)
            )
        )
        addLog("Installed High-Efficiency Solar Panel Array (+20kWh battery capacity).", "SETTLEMENT")
        return true
    }

    suspend fun buyWindTurbine(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        val cost = 240 + (state.windTurbinesCount * 60)
        if (state.coins < cost) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - cost,
                totalExpenses = state.totalExpenses + cost,
                windTurbinesCount = state.windTurbinesCount + 1,
                batteryMax = state.batteryMax + 30f,
                ecoHarmonyScore = (state.ecoHarmonyScore + 5).coerceAtMost(100)
            )
        )
        addLog("Erected Micro Wind Turbine for round-the-clock clean energy.", "SETTLEMENT")
        return true
    }

    suspend fun buyRainCollector(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        val cost = 120 + (state.rainCollectorsCount * 30)
        if (state.coins < cost) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - cost,
                totalExpenses = state.totalExpenses + cost,
                rainCollectorsCount = state.rainCollectorsCount + 1,
                waterMax = state.waterMax + 50f,
                ecoHarmonyScore = (state.ecoHarmonyScore + 3).coerceAtMost(100)
            )
        )
        addLog("Built Rainwater Harvesting Silo (+50L clean water storage).", "SETTLEMENT")
        return true
    }

    suspend fun upgradeSettlementTier(): Boolean = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return false
        val nextLevel = state.settlementTier.level + 1
        val nextTier = SettlementTier.values().find { it.level == nextLevel } ?: return false

        if (state.coins < nextTier.requiredCoins) return false
        if (state.ecoHarmonyScore < nextTier.requiredEcoScore) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - nextTier.requiredCoins,
                totalExpenses = state.totalExpenses + nextTier.requiredCoins,
                settlementTier = nextTier,
                ecoHarmonyScore = (state.ecoHarmonyScore + 10).coerceAtMost(100)
            )
        )

        // Expand crop plots when tier upgrades
        val currentPlots = dao.getAllPlotsDirect()
        if (currentPlots.size < 12 && nextLevel >= 3) {
            val newPlots = (currentPlots.size + 1..12).map { id ->
                CropPlotEntity(id = id, cropType = null, growthProgress = 0f, waterLevel = 0.8f, soilFertility = 1.1f)
            }
            dao.insertPlots(newPlots)
        }

        addLog("🏆 SETTLEMENT UPGRADE: Advanced to ${nextTier.title}! ${nextTier.perks}", "SETTLEMENT")
        return true
    }

    suspend fun buySeeds(seedItem: ItemId, quantity: Int): Boolean = stateMutex.withLock {
        if (quantity <= 0) return false
        if (seedItem.category != ItemCategory.SEEDS) return false
        val state = dao.getFarmStateDirect() ?: return false
        val totalCost = seedItem.basePrice * quantity
        if (state.coins < totalCost) return false

        dao.insertOrUpdateFarmState(
            state.copy(
                coins = state.coins - totalCost,
                totalExpenses = state.totalExpenses + totalCost
            )
        )
        dao.addInventoryQuantity(seedItem, quantity)
        addLog("Purchased $quantity ${seedItem.displayName} packets for $totalCost Coins.", "MARKET")
        return true
    }

    // --- MARKET SYSTEM & QUOTES ---

    suspend fun recalculateDailyMarketQuotes(day: Int): List<MarketQuoteEntity> = stateMutex.withLock {
        return recalculateDailyMarketQuotesInternal(day)
    }

    private suspend fun recalculateDailyMarketQuotesInternal(day: Int): List<MarketQuoteEntity> {
        val state = dao.getFarmStateDirect() ?: return emptyList()
        val weather = state.weather
        val season = state.season
        val reputationMultiplier = 1.0f + (state.businessReputation / 300f)

        val quotes = MONITORED_MARKET_ITEMS.map { item ->
            var eventMultiplier = 1.0f
            var driver = "Standard commerce"
            var demand = MarketDemand.NORMAL

            when {
                weather == WeatherType.DROUGHT && (item == ItemId.TOMATO || item == ItemId.CARROT || item == ItemId.CORN || item == ItemId.WHEAT) -> {
                    eventMultiplier = 1.65f
                    driver = "Valley drought: severe crop scarcity (+65%)"
                    demand = MarketDemand.HIGH
                }
                weather == WeatherType.HEATWAVE && (item == ItemId.STRAWBERRY || item == ItemId.SOLAR_JUICE || item == ItemId.MINT) -> {
                    eventMultiplier = 1.50f
                    driver = "Heatwave: cold refreshments surge (+50%)"
                    demand = MarketDemand.HIGH
                }
                season == Season.WINTER && (item == ItemId.SHEEP_WOOL || item == ItemId.HANDMADE_BLANKET || item == ItemId.ARTISAN_BREAD || item == ItemId.HERBAL_BALM) -> {
                    eventMultiplier = 1.60f
                    driver = "Winter freeze: warm textiles & bread surge (+60%)"
                    demand = MarketDemand.HIGH
                }
                season == Season.AUTUMN && (item == ItemId.WHEAT || item == ItemId.CORN || item == ItemId.ARTISAN_CHEESE) -> {
                    eventMultiplier = 1.35f
                    driver = "Harvest festival: bulk buying bonus (+35%)"
                    demand = MarketDemand.HIGH
                }
                season == Season.SPRING && (item == ItemId.EGGS || item == ItemId.DUCK_EGGS || item == ItemId.HONEY || item == ItemId.MINT) -> {
                    eventMultiplier = 1.25f
                    driver = "Spring kitchen renewal demand (+25%)"
                    demand = MarketDemand.NORMAL
                }
                item == ItemId.PACKAGED_MEAT || item == ItemId.ARTISAN_CHEESE || item == ItemId.SMOKED_FISH || item == ItemId.PREMIUM_HONEY -> {
                    eventMultiplier = 1.20f
                    driver = "High-margin processed artisan demand (+20%)"
                    demand = MarketDemand.NORMAL
                }
            }

            val totalMultiplier = (eventMultiplier * reputationMultiplier).coerceIn(0.70f, 1.80f)
            val currentPrice = (item.basePrice * totalMultiplier).toInt().coerceAtLeast(1)
            val percentChange = (((currentPrice.toFloat() / item.basePrice) - 1.0f) * 100).toInt()

            MarketQuoteEntity(
                itemId = item,
                basePrice = item.basePrice,
                currentPrice = currentPrice,
                priceChangePercent = percentChange,
                demand = if (totalMultiplier >= 1.25f) MarketDemand.HIGH else if (totalMultiplier <= 0.85f) MarketDemand.LOW else MarketDemand.NORMAL,
                marketDriver = driver,
                dayCalculated = day
            )
        }

        dao.insertOrUpdateMarketQuotes(quotes)
        return quotes
    }

    suspend fun getMarketQuotes(): List<MarketItemQuote> {
        val quotes = dao.getAllMarketQuotesDirect().ifEmpty {
            val state = dao.getFarmStateDirect()
            recalculateDailyMarketQuotes(state?.day ?: 1)
        }
        return quotes.map {
            MarketItemQuote(
                itemId = it.itemId,
                basePrice = it.basePrice,
                currentPrice = it.currentPrice,
                priceChangePercent = it.priceChangePercent,
                demand = it.demand,
                marketDriver = it.marketDriver
            )
        }
    }

    // --- BUSINESS PROGRESSION ADVANCEMENT ---
    suspend fun advanceBusinessLevel(): BusinessLevel? = stateMutex.withLock {
        val state = dao.getFarmStateDirect() ?: return null
        val nextLevelNumber = state.businessLevel.level + 1
        val nextLevel = BusinessLevel.values().find { it.level == nextLevelNumber } ?: return null

        if (state.totalEarnings < nextLevel.requiredTotalRevenue || state.businessReputation < nextLevel.requiredReputation) {
            return null
        }

        dao.insertOrUpdateFarmState(
            state.copy(
                businessLevel = nextLevel,
                ecoHarmonyScore = (state.ecoHarmonyScore + 5).coerceAtMost(100),
                businessReputation = (state.businessReputation + 5).coerceAtMost(100)
            )
        )
        addLog("📈 BUSINESS EXPANSION: Promoted to ${nextLevel.title}! ${nextLevel.perkDescription}", "SHOP")
        return nextLevel
    }
}
