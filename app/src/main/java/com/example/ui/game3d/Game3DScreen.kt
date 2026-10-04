package com.example.ui.game3d

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AnimalEntity
import com.example.data.local.ContractEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LogMessageEntity
import com.example.data.local.MarketQuoteEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.CraftingRecipe
import com.example.data.model.CropType
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.game3d.SolarpunkGame
import com.example.game3d.data.GameWorldSnapshot
import com.example.game3d.data.InteractableTarget
import com.example.game3d.data.InteractableType
import com.example.ui.components.FarmTopHud
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EcoShopScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.SolarEnergyScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

enum class ActiveOverlayScreen {
    NONE,
    WORKSHOP,
    ECO_SHOP,
    MARKET,
    SOLAR_GRID,
    DASHBOARD
}

@Composable
fun Game3DOverlay(
    game: SolarpunkGame,
    farmState: FarmStateEntity?,
    animals: List<AnimalEntity>,
    plots: List<CropPlotEntity>,
    inventory: List<InventoryEntity>,
    workshopQueue: List<WorkshopQueueEntity>,
    shelves: List<ShopShelfEntity>,
    contracts: List<ContractEntity>,
    logs: List<LogMessageEntity>,
    gameSpeed: Float,
    marketQuotes: List<MarketQuoteEntity>,
    onHarvestPlot: (Int) -> Unit,
    onWaterPlot: (Int) -> Unit,
    onWaterPlots: () -> Unit,
    onFertilizePlot: (Int) -> Unit,
    onPlantCrop: (Int, CropType) -> Unit,
    onFeedAnimals: () -> Unit,
    onWaterAnimals: () -> Unit,
    onCollectProduce: (Long) -> Unit,
    onCollectAllProduce: () -> Unit,
    onBreedAnimal: (Long) -> Unit,
    onSellAnimal: (Long) -> Unit,
    onProcessAnimal: (Long) -> Unit,
    onStartCrafting: (CraftingRecipe) -> Unit,
    onCollectWorkshop: (Long) -> Unit,
    onStockShelf: (Int, ItemId, Int) -> Unit,
    onClearShelf: (Int) -> Unit,
    onUpdateShelfPricing: (Int, PricingStrategy) -> Unit,
    onFulfillContract: (String) -> Unit,
    onSellWholesale: (ItemId, Int) -> Unit,
    onBuySeeds: (ItemId, Int) -> Unit,
    onProcessCompost: () -> Unit,
    onUpgradeSolarPanels: () -> Unit,
    onUpgradeWindTurbines: () -> Unit,
    onUpgradeRainCollectors: () -> Unit,
    onUpgradeSettlement: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    // Synchronize snapshot into 3D game
    LaunchedEffect(farmState, animals, plots, inventory, shelves) {
        if (farmState != null) {
            game.updateWorldSnapshot(
                GameWorldSnapshot(
                    farmState = farmState,
                    animals = animals,
                    plots = plots,
                    inventory = inventory,
                    shelves = shelves,
                    day = farmState.day,
                    hour = farmState.hour,
                    season = farmState.season,
                    weather = farmState.weather,
                    solarEnergy = farmState.solarEnergy,
                    batteryStored = farmState.batteryStored,
                    windTurbinesCount = farmState.windTurbinesCount,
                    solarPanelsCount = farmState.solarPanelsCount
                )
            )
        }
    }

    val currentTarget by game.interactionSystem.currentTarget.collectAsStateWithLifecycle()

    var isRunning by remember { mutableStateOf(false) }
    var activeOverlay by remember { mutableStateOf(ActiveOverlayScreen.NONE) }
    var selectedAnimalForModal by remember { mutableStateOf<AnimalEntity?>(null) }
    var selectedPlotForPlantModal by remember { mutableStateOf<Int?>(null) }
    var showInventoryModal by remember { mutableStateOf(false) }

    // Intercept Back button when an overlay is active
    BackHandler(enabled = activeOverlay != ActiveOverlayScreen.NONE || showInventoryModal || selectedAnimalForModal != null || selectedPlotForPlantModal != null) {
        if (selectedAnimalForModal != null) selectedAnimalForModal = null
        else if (selectedPlotForPlantModal != null) selectedPlotForPlantModal = null
        else if (showInventoryModal) showInventoryModal = false
        else activeOverlay = ActiveOverlayScreen.NONE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_3d_root")
    ) {
        // Camera Touch Drag & Pinch-to-Zoom Surface
        CameraTouchArea(
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_touch_area"),
            enabled = activeOverlay == ActiveOverlayScreen.NONE && !showInventoryModal && selectedAnimalForModal == null && selectedPlotForPlantModal == null,
            onRotate = { dx, dy -> game.inputState.addLookDelta(dx, dy) },
            onZoom = { delta -> game.inputState.addZoom(delta) }
        )

        // Main Transparent HUD Overlay Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HUD (Coins, Energy, Time, Weather, Menu buttons)
            if (farmState != null) {
                FarmTopHud(
                    state = farmState,
                    gameSpeed = gameSpeed,
                    onSpeedChange = onSpeedChange
                )
            } else {
                Spacer(modifier = Modifier.height(56.dp))
            }

            // BOTTOM CONTROLS (Joystick on Left, Proximity Card, Action & Run buttons on Right)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Floating Context Action Prompt Card
                ContextActionPrompt(
                    target = currentTarget,
                    modifier = Modifier.padding(bottom = 12.dp),
                    onActionClicked = { target ->
                        when (target.type) {
                            InteractableType.CROP_PLOT -> {
                                val plot = plots.find { it.id == target.plotIndex }
                                if (plot != null) {
                                    if (plot.cropType == null) {
                                        selectedPlotForPlantModal = plot.id
                                    } else if (plot.isReadyForHarvest) {
                                        onHarvestPlot(plot.id)
                                    } else {
                                        onWaterPlot(plot.id)
                                    }
                                }
                            }
                            InteractableType.ANIMAL -> {
                                val animal = animals.find { it.id == target.entityId }
                                if (animal != null) {
                                    if (animal.produceReady) {
                                        onCollectProduce(animal.id)
                                    } else {
                                        selectedAnimalForModal = animal
                                    }
                                }
                            }
                            InteractableType.WORKSHOP -> activeOverlay = ActiveOverlayScreen.WORKSHOP
                            InteractableType.ECO_SHOP -> activeOverlay = ActiveOverlayScreen.ECO_SHOP
                            InteractableType.MARKET -> activeOverlay = ActiveOverlayScreen.MARKET
                            InteractableType.SOLAR_STATION -> activeOverlay = ActiveOverlayScreen.SOLAR_GRID
                            InteractableType.COMPOSTER -> onProcessCompost()
                            InteractableType.FARM_HOUSE -> activeOverlay = ActiveOverlayScreen.DASHBOARD
                            InteractableType.NPC -> activeOverlay = ActiveOverlayScreen.MARKET
                        }
                    }
                )

                // Virtual Joystick (Left) & Controls/Actions (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // LEFT: Virtual Joystick
                    VirtualJoystick(
                        size = 140.dp,
                        onMove = { x, y -> game.inputState.setMovement(x, y) }
                    )

                    // RIGHT: Backpack, Run Toggle, and Primary Action FAB
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Backpack Inventory Button
                        SmallFloatingActionButton(
                            onClick = { showInventoryModal = true },
                            containerColor = Color(0xDD122019),
                            contentColor = SolarSunAmber,
                            shape = CircleShape,
                            modifier = Modifier.testTag("backpack_button")
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = "Inventory", modifier = Modifier.size(20.dp))
                        }

                        // Run / Walk Toggle Button
                        SmallFloatingActionButton(
                            onClick = {
                                isRunning = !isRunning
                                game.inputState.isRunning = isRunning
                            },
                            containerColor = if (isRunning) SolarSunAmber else Color(0xDD122019),
                            contentColor = if (isRunning) Color(0xFF1E293B) else Color.White,
                            shape = CircleShape,
                            modifier = Modifier.testTag("run_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
                                contentDescription = if (isRunning) "Running" else "Walking",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Primary Context Action Button (Interacting with current target)
                        FloatingActionButton(
                            onClick = {
                                currentTarget?.let { target ->
                                    when (target.type) {
                                        InteractableType.CROP_PLOT -> {
                                            val plot = plots.find { it.id == target.plotIndex }
                                            if (plot != null) {
                                                if (plot.cropType == null) {
                                                    selectedPlotForPlantModal = plot.id
                                                } else if (plot.isReadyForHarvest) {
                                                    onHarvestPlot(plot.id)
                                                } else {
                                                    onWaterPlot(plot.id)
                                                }
                                            }
                                        }
                                        InteractableType.ANIMAL -> {
                                            val animal = animals.find { it.id == target.entityId }
                                            if (animal != null) {
                                                if (animal.produceReady) {
                                                    onCollectProduce(animal.id)
                                                } else {
                                                    selectedAnimalForModal = animal
                                                }
                                            }
                                        }
                                        InteractableType.WORKSHOP -> activeOverlay = ActiveOverlayScreen.WORKSHOP
                                        InteractableType.ECO_SHOP -> activeOverlay = ActiveOverlayScreen.ECO_SHOP
                                        InteractableType.MARKET -> activeOverlay = ActiveOverlayScreen.MARKET
                                        InteractableType.SOLAR_STATION -> activeOverlay = ActiveOverlayScreen.SOLAR_GRID
                                        InteractableType.COMPOSTER -> onProcessCompost()
                                        InteractableType.FARM_HOUSE -> activeOverlay = ActiveOverlayScreen.DASHBOARD
                                        InteractableType.NPC -> activeOverlay = ActiveOverlayScreen.MARKET
                                    }
                                }
                            },
                            containerColor = if (currentTarget != null) SolarpunkEmerald else Color(0x6610B981),
                            contentColor = Color(0xFF064E3B),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("primary_action_fab")
                        ) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = "Interact",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- CONTEXTUAL MODALS ---

        // Animal Care & Valuation Sheet
        selectedAnimalForModal?.let { animal ->
            AnimalDetailModal(
                animal = animal,
                onDismiss = { selectedAnimalForModal = null },
                onFeed = {
                    onFeedAnimals()
                    game.playAnimalInteractionSound(animal.id)
                },
                onWater = {
                    onWaterAnimals()
                    game.playAnimalInteractionSound(animal.id)
                },
                onCollectProduce = {
                    onCollectProduce(animal.id)
                    game.playAnimalInteractionSound(animal.id)
                },
                onBreed = {
                    onBreedAnimal(animal.id)
                    game.playAnimalInteractionSound(animal.id)
                },
                onSell = onSellAnimal,
                onProcess = onProcessAnimal
            )
        }

        // Crop Planting Seed Picker
        selectedPlotForPlantModal?.let { plotId ->
            CropPlantModal(
                plotId = plotId,
                inventory = inventory,
                onDismiss = { selectedPlotForPlantModal = null },
                onPlantCrop = onPlantCrop
            )
        }

        // Backpack Quick Inventory Modal
        if (showInventoryModal) {
            QuickInventoryModal(
                inventory = inventory,
                onDismiss = { showInventoryModal = false }
            )
        }

        // --- FULLSCREEN OVERLAYS (WORKSHOP, SHOP, MARKET, GRID, DASHBOARD) ---

        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlayScreen.WORKSHOP,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                WorkshopScreen(
                    state = farmState,
                    workshopQueue = workshopQueue,
                    inventory = inventory,
                    onStartCrafting = onStartCrafting,
                    onCollectTask = onCollectWorkshop
                )
            }
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlayScreen.ECO_SHOP,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                EcoShopScreen(
                    state = farmState,
                    shelves = shelves,
                    contracts = contracts,
                    inventory = inventory,
                    onStockShelf = onStockShelf,
                    onClearShelf = onClearShelf,
                    onUpdatePricing = onUpdateShelfPricing,
                    onFulfillContract = onFulfillContract
                )
            }
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlayScreen.MARKET,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                MarketScreen(
                    state = farmState,
                    inventory = inventory,
                    marketQuotes = marketQuotes,
                    onWholesaleSell = onSellWholesale,
                    onGoFishing = { /* Fish action handled in MarketScreen */ },
                    onActivateAquaponics = { /* Aquaponics action */ },
                    onBuySeeds = onBuySeeds
                )
            }
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlayScreen.SOLAR_GRID,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                SolarEnergyScreen(
                    state = farmState,
                    onBuySolarPanel = onUpgradeSolarPanels,
                    onBuyWindTurbine = onUpgradeWindTurbines,
                    onBuyRainCollector = onUpgradeRainCollectors,
                    onUpgradeSettlementTier = onUpgradeSettlement
                )
            }
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlayScreen.DASHBOARD,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                DashboardScreen(
                    state = farmState,
                    animals = animals,
                    plots = plots,
                    workshops = workshopQueue,
                    shelves = shelves,
                    logs = logs,
                    onFeedAnimals = onFeedAnimals,
                    onWaterPlots = onWaterPlots,
                    onCollectProduce = onCollectAllProduce,
                    onCompostManure = onProcessCompost,
                    onUpgradeSettlement = onUpgradeSettlement,
                    onNavigateTab = { /* Tab nav inside dashboard */ }
                )
            }
        }
    }
}
