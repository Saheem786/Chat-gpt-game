package com.example

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.example.game3d.SolarpunkGame
import com.example.game3d.SolarpunkGameFragment
import com.example.ui.FarmViewModel
import com.example.ui.game3d.Game3DOverlay
import com.example.ui.theme.SolarpunkFarmTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : FragmentActivity(), AndroidFragmentApplication.Callbacks {

    private val viewModel: FarmViewModel by viewModels()

    val game = SolarpunkGame(
        onPlayerPositionChanged = { x, y, z, yaw ->
            viewModel.savePlayerPosition(x, y, z, yaw)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Attach LibGDX 3D Game Fragment
        if (savedInstanceState == null) {
            val fragment = SolarpunkGameFragment().apply {
                this.game = this@MainActivity.game
            }
            supportFragmentManager.commit {
                replace(R.id.game_fragment_container, fragment)
            }
        }

        // 2. Attach Transparent Jetpack Compose 3D Overlay
        val composeOverlay = findViewById<ComposeView>(R.id.compose_overlay)
        composeOverlay.setContent {
            SolarpunkFarmTheme {
                Main3DApp(viewModel = viewModel, game = game)
            }
        }
    }

    override fun exit() {
        finish()
    }
}

@Composable
fun Main3DApp(viewModel: FarmViewModel, game: SolarpunkGame) {
    val farmState by viewModel.farmState.collectAsStateWithLifecycle()
    val animals by viewModel.animals.collectAsStateWithLifecycle()
    val plots by viewModel.plots.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val workshopQueue by viewModel.workshopQueue.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val contracts by viewModel.contracts.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val gameSpeed by viewModel.gameSpeed.collectAsStateWithLifecycle()
    val marketQuotes by viewModel.marketQuotes.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userFeedback.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Game3DOverlay(
            game = game,
            farmState = farmState,
            animals = animals,
            plots = plots,
            inventory = inventory,
            workshopQueue = workshopQueue,
            shelves = shelves,
            contracts = contracts,
            logs = logs,
            gameSpeed = gameSpeed,
            marketQuotes = marketQuotes,
            onHarvestPlot = { plotId -> viewModel.harvestCrop(plotId) },
            onWaterPlot = { plotId -> viewModel.waterPlot(plotId) },
            onWaterPlots = { viewModel.waterAllPlots() },
            onFertilizePlot = { plotId -> viewModel.fertilizePlot(plotId) },
            onPlantCrop = { plotId, cropType -> viewModel.plantCrop(plotId, cropType) },
            onFeedAnimals = { viewModel.feedAnimals() },
            onWaterAnimals = { viewModel.waterAnimals() },
            onCollectProduce = { animalId -> viewModel.collectProduce(animalId) },
            onCollectAllProduce = { viewModel.collectAllProduce() },
            onBreedAnimal = { animalId -> viewModel.breedAnimal(animalId) },
            onSellAnimal = { animalId -> viewModel.sellAnimal(animalId) },
            onProcessAnimal = { animalId -> viewModel.processAnimalMeat(animalId) },
            onStartCrafting = { recipe -> viewModel.startCrafting(recipe) },
            onCollectWorkshop = { taskId -> viewModel.collectWorkshopTask(taskId) },
            onStockShelf = { shelfId, itemId, qty -> viewModel.stockShelf(shelfId, itemId, qty) },
            onClearShelf = { shelfId -> viewModel.clearShelf(shelfId) },
            onUpdateShelfPricing = { shelfId, strategy -> viewModel.updateShelfPricing(shelfId, strategy) },
            onFulfillContract = { contractId -> viewModel.fulfillContract(contractId) },
            onSellWholesale = { itemId, qty -> viewModel.wholesaleSell(itemId, qty) },
            onBuySeeds = { seedItem, qty -> viewModel.buySeeds(seedItem, qty) },
            onProcessCompost = { viewModel.compostManure() },
            onUpgradeSolarPanels = { viewModel.buySolarPanel() },
            onUpgradeWindTurbines = { viewModel.buyWindTurbine() },
            onUpgradeRainCollectors = { viewModel.buyRainCollector() },
            onUpgradeSettlement = { viewModel.upgradeSettlementTier() },
            onSpeedChange = { speed -> viewModel.setSpeed(speed) }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .statusBarsPadding()
                .navigationBarsPadding()
        )
    }
}
