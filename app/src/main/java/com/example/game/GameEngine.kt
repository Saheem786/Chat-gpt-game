package com.example.game

import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmDao
import com.example.data.local.FarmStateEntity
import com.example.data.local.LogMessageEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.data.model.Season
import com.example.data.model.WeatherType
import com.example.data.model.WorkshopRecipes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameEngine(
    private val dao: FarmDao,
    private val scope: CoroutineScope
) {
    private var simulationJob: Job? = null

    private val _gameSpeed = MutableStateFlow(1f) // 0 = paused, 1 = normal, 2 = fast, 5 = ultra
    val gameSpeed: StateFlow<Float> = _gameSpeed.asStateFlow()

    private val customerNames = listOf("Elena", "Mateo", "Priya", "Lucas", "Amina", "Oliver", "Zara", "Kenji", "Hanna", "Liam")

    fun start() {
        if (simulationJob?.isActive == true) return
        simulationJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val speed = _gameSpeed.value
                if (speed > 0f) {
                    val delayMs = (2400L / speed).toLong().coerceAtLeast(400L)
                    delay(delayMs)
                    processGameHourTick()
                } else {
                    delay(1000L)
                }
            }
        }
    }

    fun setSpeed(speed: Float) {
        _gameSpeed.value = speed
    }

    private suspend fun processGameHourTick() {
        val currentState = dao.getFarmStateDirect() ?: return

        // 1. Time advancement
        var newHour = currentState.hour + 1
        var newDay = currentState.day
        if (newHour >= 24) {
            newHour = 0
            newDay += 1
        }

        // Season cycle: 15 game days per season
        val seasonIndex = ((newDay - 1) / 15) % 4
        val currentSeason = Season.values()[seasonIndex]

        // 2. Weather cycle
        var weatherHoursLeft = currentState.weatherHoursRemaining - 1
        var currentWeather = currentState.weather
        if (weatherHoursLeft <= 0) {
            currentWeather = pickNextWeather(currentSeason)
            weatherHoursLeft = Random.nextInt(6, 16)
            dao.insertLog(
                LogMessageEntity(
                    day = newDay,
                    hour = newHour,
                    message = "Weather changed to ${currentWeather.displayName} ${currentWeather.emoji}",
                    category = "WEATHER"
                )
            )
        }

        // 3. Clean Energy & Water Simulation
        val isDaytime = newHour in 6..19
        val solarGen = if (isDaytime) {
            currentState.solarPanelsCount * 2.5f * currentWeather.solarMultiplier
        } else 0f

        val windGen = currentState.windTurbinesCount * 1.8f * currentWeather.windMultiplier
        val totalGen = solarGen + windGen

        val baseDrain = 1.2f + (currentState.compostBinsActive * 0.4f)
        val netEnergy = totalGen - baseDrain
        val newBattery = (currentState.batteryStored + netEnergy).coerceIn(0f, currentState.batteryMax)

        var newWater = currentState.waterStored
        if (currentWeather.isRaining) {
            newWater = (newWater + (currentState.rainCollectorsCount * 5f)).coerceAtMost(currentState.waterMax)
        }

        // 4. Livestock Life Cycle
        val animals = dao.getAllAnimalsDirect()
        val updatedAnimals = mutableListOf<AnimalEntity>()
        var babyBornSpecies: AnimalSpecies? = null

        animals.forEach { animal ->
            val thirstInc = if (currentWeather == WeatherType.HEATWAVE) 0.06f else 0.03f
            val newThirst = (animal.thirst + thirstInc).coerceAtMost(1f)
            val newHunger = (animal.hunger + 0.03f).coerceAtMost(1f)

            // Health is affected if starving or dehydrated
            val healthDelta = if (newThirst > 0.8f || newHunger > 0.8f) -0.05f else 0.02f
            val newHealth = (animal.health + healthDelta).coerceIn(0.2f, 1.0f)

            val happyDelta = if (newThirst < 0.3f && newHunger < 0.3f) 0.03f else -0.04f
            val newHappiness = (animal.happiness + happyDelta).coerceIn(0.1f, 1.0f)

            // Production countdown
            var countdown = animal.hoursUntilProduce - 1
            var ready = animal.produceReady
            if (countdown <= 0 && !ready && newHealth > 0.5f) {
                ready = true
                countdown = 0
            }

            // Small natural breeding chance if well-cared
            if (animal.happiness > 0.85f && animal.health > 0.85f && animals.size < 18 && Random.nextFloat() < 0.008f) {
                babyBornSpecies = animal.species
            }

            updatedAnimals.add(
                animal.copy(
                    hunger = newHunger,
                    thirst = newThirst,
                    health = newHealth,
                    happiness = newHappiness,
                    hoursUntilProduce = countdown.coerceAtLeast(0),
                    produceReady = ready
                )
            )
        }
        if (updatedAnimals.isNotEmpty()) {
            dao.updateAnimals(updatedAnimals)
        }

        babyBornSpecies?.let { species ->
            dao.insertAnimal(
                AnimalEntity(
                    species = species,
                    nickname = "Little ${species.displayName} #${Random.nextInt(10, 99)}",
                    hunger = 0.1f,
                    thirst = 0.1f,
                    health = 1f,
                    happiness = 1f,
                    ageDays = 0
                )
            )
            dao.insertLog(
                LogMessageEntity(
                    day = newDay,
                    hour = newHour,
                    message = "🎉 Miracle of Life! A healthy baby ${species.displayName} was born in your sanctuary!",
                    category = "ANIMALS"
                )
            )
        }

        // 5. Crop Plots Growth & Free Rain Irrigation
        val plots = dao.getAllPlotsDirect()
        val updatedPlots = plots.map { plot ->
            var water = plot.waterLevel
            if (currentWeather.isRaining && !plot.hasGreenhouse) {
                water = (water + 0.3f).coerceAtMost(1f)
            } else if (plot.cropType != null) {
                val consumption = if (plot.hasGreenhouse) 0.02f else (if (currentWeather == WeatherType.HEATWAVE) 0.06f else 0.03f)
                water = (water - consumption).coerceAtLeast(0f)
            }

            var progress = plot.growthProgress
            var ready = plot.isReadyForHarvest
            if (plot.cropType != null && water > 0.1f && !ready) {
                val crop = plot.cropType
                val speed = (1.0f / crop.growthHours) * plot.soilFertility * (if (currentSeason == crop.preferredSeason) 1.25f else 1.0f)
                progress = (progress + speed).coerceAtMost(1.0f)
                if (progress >= 1.0f) {
                    ready = true
                }
            }
            plot.copy(waterLevel = water, growthProgress = progress, isReadyForHarvest = ready)
        }
        dao.updatePlots(updatedPlots)

        // 6. Workshop Queue Processing
        val tasks = dao.getWorkshopQueueDirect()
        tasks.filter { !it.isFinished }.forEach { task ->
            val newProgress = task.progressHours + 1f
            if (newProgress >= task.totalHours) {
                dao.updateWorkshopTask(task.copy(progressHours = newProgress, isFinished = true))
                val recipe = WorkshopRecipes.ALL.find { it.id == task.recipeId }
                dao.insertLog(
                    LogMessageEntity(
                        day = newDay,
                        hour = newHour,
                        message = "✨ Workshop Finished: ${recipe?.name ?: "Artisan Product"} is ready for collection!",
                        category = "WORKSHOP"
                    )
                )
            } else {
                dao.updateWorkshopTask(task.copy(progressHours = newProgress))
            }
        }

        // 7. Retail Eco-Shop: Animated Customer Purchases
        var extraEarnings = 0
        if (newHour in 8..20) { // Store open daytime
            val shelves = dao.getAllShelvesDirect().filter { it.stockedItemId != null && it.quantity > 0 }
            if (shelves.isNotEmpty()) {
                // 40% chance of customer visit each hour
                if (Random.nextFloat() < 0.45f) {
                    val shelf = shelves.random()
                    val item = shelf.stockedItemId!!
                    val customer = customerNames.random()

                    val basePrice = item.basePrice
                    val multiplier = shelf.pricingStrategy.priceMultiplier
                    val unitPrice = (basePrice * multiplier).toInt().coerceAtLeast(1)

                    // Customer buy probability based on pricing & eco-harmony
                    val appeal = shelf.pricingStrategy.customerAppealMultiplier * (currentState.ecoHarmonyScore / 50f)
                    if (Random.nextFloat() < appeal) {
                        val qtySold = Random.nextInt(1, 3).coerceAtMost(shelf.quantity)
                        val revenue = unitPrice * qtySold

                        dao.updateShelf(shelf.copy(quantity = shelf.quantity - qtySold))
                        extraEarnings += revenue

                        dao.insertLog(
                            LogMessageEntity(
                                day = newDay,
                                hour = newHour,
                                message = "🏪 Eco-Shop: $customer purchased $qtySold ${item.displayName} (+$revenue Coins).",
                                category = "SHOP"
                            )
                        )
                    }
                }
            }
        }

        // 8. River Fish Natural Regeneration & Aquaponics
        var fishHealth = (currentState.fishPopulationHealth + 0.005f).coerceAtMost(1.0f)
        if (currentState.aquaponicsActive && newHour % 8 == 0) {
            // Aquaponics supplies free tilapia & seaweed sustainably!
            dao.addInventoryQuantity(ItemId.TILAPIA, 1)
            dao.addInventoryQuantity(ItemId.MINT, 1)
        }

        // 9. Eco-Harmony Index recalculation
        val renewableRatio = (currentState.solarPanelsCount * 10 + currentState.windTurbinesCount * 15).coerceAtMost(40)
        val animalCareBonus = (animals.map { it.happiness }.average().takeIf { !it.isNaN() } ?: 0.5) * 25
        val soilHealthBonus = (plots.map { it.soilFertility }.average().takeIf { !it.isNaN() } ?: 1.0) * 15
        val calculatedHarmony = (renewableRatio + animalCareBonus + soilHealthBonus + (if (currentState.aquaponicsActive) 10 else 0)).toInt().coerceIn(10, 100)

        // Check for expired wholesale contracts
        var updatedReputation = currentState.businessReputation
        if (newHour == 0) {
            val contracts = dao.getAllContractsDirect()
            contracts.filter { !it.isCompleted && it.expiryDay < newDay }.forEach { expired ->
                updatedReputation = (updatedReputation - 4).coerceAtLeast(10)
                dao.insertLog(
                    LogMessageEntity(
                        day = newDay,
                        hour = newHour,
                        message = "⚠️ Contract Expired: Missed deadline for ${expired.clientName}. Business reputation decreased (-4).",
                        category = "SHOP"
                    )
                )
            }
        }

        val updatedSalesToday = if (newHour == 0 && currentState.hour == 23) extraEarnings else (currentState.salesToday + extraEarnings)

        // Save updated Farm State
        dao.insertOrUpdateFarmState(
            currentState.copy(
                coins = currentState.coins + extraEarnings,
                day = newDay,
                hour = newHour,
                season = currentSeason,
                weather = currentWeather,
                weatherHoursRemaining = weatherHoursLeft,
                batteryStored = newBattery,
                waterStored = newWater,
                fishPopulationHealth = fishHealth,
                ecoHarmonyScore = calculatedHarmony,
                totalEarnings = currentState.totalEarnings + extraEarnings,
                salesToday = updatedSalesToday,
                businessReputation = updatedReputation
            )
        )
    }

    private fun pickNextWeather(season: Season): WeatherType {
        val roll = Random.nextFloat()
        return when (season) {
            Season.SPRING -> when {
                roll < 0.45f -> WeatherType.SUNNY
                roll < 0.75f -> WeatherType.RAINY
                else -> WeatherType.PARTLY_CLOUDY
            }
            Season.SUMMER -> when {
                roll < 0.50f -> WeatherType.SUNNY
                roll < 0.80f -> WeatherType.HEATWAVE
                else -> WeatherType.DROUGHT
            }
            Season.AUTUMN -> when {
                roll < 0.40f -> WeatherType.WIND_STORM
                roll < 0.70f -> WeatherType.PARTLY_CLOUDY
                else -> WeatherType.RAINY
            }
            Season.WINTER -> when {
                roll < 0.45f -> WeatherType.WIND_STORM
                roll < 0.80f -> WeatherType.PARTLY_CLOUDY
                else -> WeatherType.RAINY
            }
        }
    }
}
