package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FarmViewModel
import com.example.ui.components.FarmTopHud
import com.example.ui.screens.AgricultureScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EcoShopScreen
import com.example.ui.screens.LivestockScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.SolarEnergyScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald
import com.example.ui.theme.SolarpunkFarmTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: FarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolarpunkFarmTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: FarmViewModel) {
    val farmState by viewModel.farmState.collectAsStateWithLifecycle()
    val animals by viewModel.animals.collectAsStateWithLifecycle()
    val plots by viewModel.plots.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val workshopQueue by viewModel.workshopQueue.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val contracts by viewModel.contracts.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val gameSpeed by viewModel.gameSpeed.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var marketSubTab by remember { mutableIntStateOf(0) } // 0 = Trade/Fishery, 1 = Solar Grid
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userFeedback.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle back button: return to home tab if not on it
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                FarmTopHud(
                    state = farmState,
                    gameSpeed = gameSpeed,
                    onSpeedChange = { viewModel.setSpeed(it) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val navItems = listOf(
                    NavigationTabItem("Farm", Icons.Default.Dashboard, 0),
                    NavigationTabItem("Animals", Icons.Default.Pets, 1),
                    NavigationTabItem("Crops", Icons.Default.Grass, 2),
                    NavigationTabItem("Workshop", Icons.Default.Build, 3),
                    NavigationTabItem("Shop", Icons.Default.Storefront, 4),
                    NavigationTabItem("Market", Icons.Default.ShoppingBag, 5)
                )

                navItems.forEach { item ->
                    val isSelected = selectedTab == item.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = item.index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.testTag("nav_item_${item.label.lowercase()}")
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SolarpunkEmerald,
                            selectedTextColor = SolarpunkEmerald,
                            indicatorColor = SolarpunkEmerald.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "ScreenTransition") { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        state = farmState,
                        animals = animals,
                        plots = plots,
                        workshops = workshopQueue,
                        shelves = shelves,
                        logs = logs,
                        onFeedAnimals = { viewModel.feedAnimals() },
                        onWaterPlots = { viewModel.waterAllPlots() },
                        onCollectProduce = { viewModel.collectAllProduce() },
                        onCompostManure = { viewModel.compostManure() },
                        onUpgradeSettlement = { viewModel.upgradeSettlementTier() },
                        onNavigateTab = { target -> selectedTab = target }
                    )

                    1 -> LivestockScreen(
                        state = farmState,
                        animals = animals,
                        onFeedAnimals = { viewModel.feedAnimals() },
                        onWaterAnimals = { viewModel.waterAnimals() },
                        onCollectProduce = { viewModel.collectProduce(it) },
                        onCollectAll = { viewModel.collectAllProduce() },
                        onPetAnimal = { viewModel.petAnimal(it) },
                        onBuyAnimal = { species, name -> viewModel.buyAnimal(species, name) },
                        onSellAnimal = { viewModel.sellAnimal(it) },
                        onProcessAnimal = { viewModel.processAnimalMeat(it) },
                        onBreedAnimal = { viewModel.breedAnimal(it) }
                    )

                    2 -> AgricultureScreen(
                        state = farmState,
                        plots = plots,
                        inventory = inventory,
                        onPlantCrop = { plotId, crop -> viewModel.plantCrop(plotId, crop) },
                        onWaterPlot = { viewModel.waterPlot(it) },
                        onWaterAllPlots = { viewModel.waterAllPlots() },
                        onFertilizePlot = { viewModel.fertilizePlot(it) },
                        onHarvestPlot = { viewModel.harvestCrop(it) },
                        onHarvestAll = { viewModel.harvestAllCrops() },
                        onUpgradeGreenhouse = { viewModel.upgradeGreenhouse(it) },
                        onCompostManure = { viewModel.compostManure() },
                        onCraftBioFertilizer = { viewModel.craftBioFertilizer() }
                    )

                    3 -> WorkshopScreen(
                        workshopQueue = workshopQueue,
                        inventory = inventory,
                        onStartCrafting = { viewModel.startCrafting(it) },
                        onCollectTask = { viewModel.collectWorkshopTask(it) }
                    )

                    4 -> EcoShopScreen(
                        state = farmState,
                        shelves = shelves,
                        contracts = contracts,
                        inventory = inventory,
                        onStockShelf = { shelfId, item, qty -> viewModel.stockShelf(shelfId, item, qty) },
                        onClearShelf = { viewModel.clearShelf(it) },
                        onUpdatePricing = { shelfId, strat -> viewModel.updateShelfPricing(shelfId, strat) },
                        onFulfillContract = { viewModel.fulfillContract(it) }
                    )

                    5 -> Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-tabs for Market: Trade & Fishery vs Clean Power Grid
                        TabRow(
                            selectedTabIndex = marketSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = SolarpunkEmerald
                        ) {
                            Tab(
                                selected = marketSubTab == 0,
                                onClick = { marketSubTab = 0 },
                                text = { Text("🐟 Fishery & Trade", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = marketSubTab == 1,
                                onClick = { marketSubTab = 1 },
                                text = { Text("⚡ Renewable Grid", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                        }

                        if (marketSubTab == 0) {
                            MarketScreen(
                                state = farmState,
                                inventory = inventory,
                                onWholesaleSell = { item, qty -> viewModel.wholesaleSell(item, qty) },
                                onGoFishing = { viewModel.goFishing() },
                                onActivateAquaponics = { viewModel.activateAquaponics() },
                                onBuySeeds = { item, qty -> viewModel.buySeeds(item, qty) }
                            )
                        } else {
                            SolarEnergyScreen(
                                state = farmState,
                                onBuySolarPanel = { viewModel.buySolarPanel() },
                                onBuyWindTurbine = { viewModel.buyWindTurbine() },
                                onBuyRainCollector = { viewModel.buyRainCollector() },
                                onUpgradeSettlementTier = { viewModel.upgradeSettlementTier() }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavigationTabItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val index: Int
)
