package com.example.game

import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmDao
import com.example.data.local.FarmStateEntity
import com.example.data.local.LogMessageEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.BusinessLevel
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.data.model.Season
import com.example.data.model.WeatherType
import com.example.data.model.WorkshopRecipes
import com.example.data.repository.FarmRepository
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
    private val repository: FarmRepository,
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

    suspend fun advanceHourForTesting() {
        processGameHourTick()
    }

    suspend fun advanceDayForTesting() {
        repeat(24) {
            processGameHourTick()
        }
    }

    private suspend fun processGameHourTick() {
        val currentState = dao.getFarmStateDirect() ?: return

        // 1. Time advancement
        var newHour = currentState.hour + 1
        var newDay = currentState.day
        val isDayBoundary = newHour >= 24

        if (isDayBoundary) {
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
                    message = "Weather shifted to ${currentWeather.displayName} ${currentWeather.emoji}",
                    category = "WEATHER"
                )
            )
        }

        // 3. Clean Energy & Water Simulation
        val isDaytime = newHour in 6..19
        val solarMultiplierPerk = if (currentState.businessLevel.level >= BusinessLevel.LEVEL_7.level) 1.30f else 1.0f
        val solarGen = if (isDaytime) {
            currentState.solarPanelsCount * 2.5f * currentWeather.solarMultiplier * solarMultiplierPerk
        } else 0f

        val windGen = currentState.windTurbinesCount * 1.8f * currentWeather.windMultiplier
        val totalGen = solarGen + windGen

        val baseDrain = 1.0f + (currentState.compostBinsActive * 0.3f)
        val netEnergy = totalGen - baseDrain
        val newBattery = (currentState.batteryStored + netEnergy).coerceIn(0f, currentState.batteryMax)

        var newWater = currentState.waterStored
        if (currentWeather.isRaining) {
            newWater = (newWater + (currentState.rainCollectorsCount * 5f)).coerceAtMost(currentState.waterMax)
        }

        // 4. Livestock Biology & Lifecycle
        val animals = dao.getAllAnimalsDirect()
        val updatedAnimals = mutableListOf<AnimalEntity>()

        // Check day boundary: Age animals +1 ONLY on day rollover
        val dayAgeDelta = if (isDayBoundary) 1 else 0

        animals.forEach { animal ->
            val thirstInc = if (currentWeather == WeatherType.HEATWAVE) 0.06f else 0.03f
            val newThirst = (animal.thirst + thirstInc).coerceAtMost(1f)
            val newHunger = (animal.hunger + 0.03f).coerceAtMost(1f)

            // Health is affected if starving or dehydrated
            val healthDelta = if (newThirst > 0.8f || newHunger > 0.8f) -0.05f else 0.02f
            val newHealth = (animal.health + healthDelta).coerceIn(0.2f, 1.0f)

            val happyDelta = if (newThirst < 0.3f && newHunger < 0.3f) 0.03f else -0.04f
            val newHappiness = (animal.happiness + happyDelta).coerceIn(0.1f, 1.0f)

            // Production countdown - PIG NEVER PRODUCES RECURRING PRODUCE
            var countdown = animal.hoursUntilProduce
            var ready = animal.produceReady
            if (animal.species.primaryProduce != null) {
                countdown = animal.hoursUntilProduce - 1
                if (countdown <= 0 && !ready && newHealth > 0.4f) {
                    ready = true
                    countdown = 0
                }
            } else {
                // Pig has null primaryProduce; recurring meat is explicitly prohibited
                ready = false
                countdown = 0
            }

            // Pregnancy progression
            var isPregnant = animal.isPregnant
            var pregHours = animal.pregnancyHours
            if (isPregnant) {
                pregHours += 1
                if (pregHours >= animal.species.gestationHours) {
                    // Birth baby
                    isPregnant = false
                    pregHours = 0
                    val babyNickname = "Baby ${animal.species.displayName} #${Random.nextInt(100, 999)}"
                    dao.insertAnimal(
                        AnimalEntity(
                            species = animal.species,
                            nickname = babyNickname,
                            hunger = 0.1f,
                            thirst = 0.1f,
                            health = 1.0f,
                            happiness = 1.0f,
                            ageDays = 0,
                            isPregnant = false,
                            pregnancyHours = 0
                        )
                    )
                    dao.insertLog(
                        LogMessageEntity(
                            day = newDay,
                            hour = newHour,
                            message = "🎉 ${animal.nickname} gave birth to a healthy baby ${animal.species.displayName} ($babyNickname)!",
                            category = "ANIMALS"
                        )
                    )
                }
            }

            updatedAnimals.add(
                animal.copy(
                    ageDays = animal.ageDays + dayAgeDelta,
                    hunger = newHunger,
                    thirst = newThirst,
                    health = newHealth,
                    happiness = newHappiness,
                    hoursUntilProduce = countdown.coerceAtLeast(0),
                    produceReady = ready,
                    isPregnant = isPregnant,
                    pregnancyHours = pregHours
                )
            )
        }
        if (updatedAnimals.isNotEmpty()) {
            dao.updateAnimals(updatedAnimals)
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

        // 6. Workshop Queue Processing (Level 4 Perk: +25% crafting speed)
        val workshopSpeedMultiplier = if (currentState.businessLevel.level >= BusinessLevel.LEVEL_4.level) 1.25f else 1.0f
        val tasks = dao.getWorkshopQueueDirect()
        tasks.filter { !it.isFinished }.forEach { task ->
            val newProgress = task.progressHours + workshopSpeedMultiplier
            if (newProgress >= task.totalHours) {
                dao.updateWorkshopTask(task.copy(progressHours = task.totalHours.toFloat(), isFinished = true))
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
        if (newHour in 8..20) {
            val shelves = dao.getAllShelvesDirect().filter { it.stockedItemId != null && it.quantity > 0 }
            if (shelves.isNotEmpty()) {
                // Level 3 Perk: +25% customer traffic
                val trafficChance = if (currentState.businessLevel.level >= BusinessLevel.LEVEL_3.level) 0.55f else 0.40f
                if (Random.nextFloat() < trafficChance) {
                    val shelf = shelves.random()
                    val item = shelf.stockedItemId!!
                    val customer = customerNames.random()

                    // Authoritative market base price
                    val authoritativePrice = repository.getAuthoritativeMarketPrice(item)
                    val markupMultiplier = if (currentState.businessLevel.level >= BusinessLevel.LEVEL_8.level) 1.40f else 1.0f
                    val multiplier = shelf.pricingStrategy.priceMultiplier * markupMultiplier
                    val unitPrice = (authoritativePrice * multiplier).toInt().coerceAtLeast(1)

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
                                message = "🏪 Eco-Shop: $customer bought $qtySold ${item.displayName} for $revenue Coins (${unitPrice}c/ea).",
                                category = "SHOP"
                            )
                        )
                    }
                }
            }
        }

        // 8. River Fish Natural Regeneration & Aquaponics
        val fishHealth = (currentState.fishPopulationHealth + 0.005f).coerceAtMost(1.0f)
        if (currentState.aquaponicsActive && newHour % 8 == 0) {
            dao.addInventoryQuantity(ItemId.TILAPIA, 1)
            dao.addInventoryQuantity(ItemId.MINT, 1)
        }

        // 9. Eco-Harmony Index recalculation
        val renewableRatio = (currentState.solarPanelsCount * 10 + currentState.windTurbinesCount * 15).coerceAtMost(40)
        val animalCareBonus = (animals.map { it.happiness }.average().takeIf { !it.isNaN() } ?: 0.5) * 25
        val soilHealthBonus = (plots.map { it.soilFertility }.average().takeIf { !it.isNaN() } ?: 1.0) * 15
        val calculatedHarmony = (renewableRatio + animalCareBonus + soilHealthBonus + (if (currentState.aquaponicsActive) 10 else 0)).toInt().coerceIn(10, 100)

        // 10. Day boundary actions: contract expiry (penalized ONCE) & daily market quotes
        var updatedReputation = currentState.businessReputation
        if (isDayBoundary) {
            val contracts = dao.getAllContractsDirect()
            var penaltySum = 0
            val contractsToUpdate = contracts.map { contract ->
                if (!contract.isCompleted && !contract.isPenalized && contract.expiryDay < newDay) {
                    penaltySum += 4
                    dao.insertLog(
                        LogMessageEntity(
                            day = newDay,
                            hour = newHour,
                            message = "⚠️ Contract Expired: Missed deadline for ${contract.clientName}. Reputation decreased (-4).",
                            category = "SHOP"
                        )
                    )
                    contract.copy(isPenalized = true)
                } else {
                    contract
                }
            }
            dao.insertContracts(contractsToUpdate)
            if (penaltySum > 0) {
                updatedReputation = (updatedReputation - penaltySum).coerceAtLeast(10)
            }

            // Recalculate and persist daily market quotes
            repository.recalculateDailyMarketQuotes(newDay)
        }

        val updatedSalesToday = if (isDayBoundary) extraEarnings else (currentState.salesToday + extraEarnings)

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
