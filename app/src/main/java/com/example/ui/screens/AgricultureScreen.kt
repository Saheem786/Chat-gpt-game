package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.clickableWithRipple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.model.CropType
import com.example.data.model.ItemId
import com.example.ui.theme.SoilBrown
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun AgricultureScreen(
    state: FarmStateEntity?,
    plots: List<CropPlotEntity>,
    inventory: List<InventoryEntity>,
    onPlantCrop: (Int, CropType) -> Unit,
    onWaterPlot: (Int) -> Unit,
    onWaterAllPlots: () -> Unit,
    onFertilizePlot: (Int) -> Unit,
    onHarvestPlot: (Int) -> Unit,
    onHarvestAll: () -> Unit,
    onUpgradeGreenhouse: (Int) -> Unit,
    onCompostManure: () -> Unit,
    onCraftBioFertilizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var plantingPlotId by remember { mutableStateOf<Int?>(null) }

    val manureStock = inventory.find { it.itemId == ItemId.MANURE }?.quantity ?: 0
    val compostStock = inventory.find { it.itemId == ItemId.COMPOST }?.quantity ?: 0
    val fertilizerStock = inventory.find { it.itemId == ItemId.BIO_FERTILIZER }?.quantity ?: 0
    val biogasStock = inventory.find { it.itemId == ItemId.BIOGAS_CANISTER }?.quantity ?: 0

    val ripeCount = plots.count { it.cropType != null && (it.isReadyForHarvest || it.growthProgress >= 1f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("agriculture_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Zero-Waste Ecological Composting Station
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Recycling,
                                contentDescription = "Eco Loop",
                                tint = SolarpunkEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Zero-Waste Soil Station",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Animal Manure → Rich Compost → Bio-Fertilizer",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Inventory stock pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StockMiniBadge(emoji = "💩", label = "Manure", count = manureStock)
                        StockMiniBadge(emoji = "🍂", label = "Compost", count = compostStock)
                        StockMiniBadge(emoji = "🧪", label = "Fertilizer", count = fertilizerStock)
                        StockMiniBadge(emoji = "⚡", label = "Biogas", count = biogasStock)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCompostManure,
                            enabled = manureStock >= 3,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_process_compost")
                        ) {
                            Text("🍂 Compost (3 Manure)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onCraftBioFertilizer,
                            enabled = compostStock >= 2,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_craft_fertilizer")
                        ) {
                            Text("🧪 Craft Bio-Fertilizer", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Field Operations Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Crop Fields & Greenhouses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onWaterAllPlots,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("💧 Irrigate All", fontSize = 11.sp)
                    }

                    if (ripeCount > 0) {
                        Button(
                            onClick = onHarvestAll,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🌾 Harvest ($ripeCount)", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Plot Cards Grid
        items(plots) { plot ->
            CropPlotCard(
                plot = plot,
                canEnrich = compostStock > 0 || fertilizerStock > 0,
                userCoins = state?.coins ?: 0,
                onPlantClick = { plantingPlotId = plot.id },
                onWater = { onWaterPlot(plot.id) },
                onFertilize = { onFertilizePlot(plot.id) },
                onHarvest = { onHarvestPlot(plot.id) },
                onUpgradeGreenhouse = { onUpgradeGreenhouse(plot.id) }
            )
        }
    }

    // Plant Dialog
    plantingPlotId?.let { plotId ->
        PlantSeedDialog(
            inventory = inventory,
            onDismiss = { plantingPlotId = null },
            onSelectCrop = { crop ->
                onPlantCrop(plotId, crop)
                plantingPlotId = null
            }
        )
    }
}

@Composable
fun StockMiniBadge(emoji: String, label: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "$count", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CropPlotCard(
    plot: CropPlotEntity,
    canEnrich: Boolean,
    userCoins: Int,
    onPlantClick: () -> Unit,
    onWater: () -> Unit,
    onFertilize: () -> Unit,
    onHarvest: () -> Unit,
    onUpgradeGreenhouse: () -> Unit
) {
    val isRipe = plot.isReadyForHarvest || plot.growthProgress >= 1.0f
    val isGrowing = plot.cropType != null && !isRipe

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_plot_${plot.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Plot #, Status, Greenhouse badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Plot #${plot.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (plot.hasGreenhouse) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00897B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "☀️ Solar Greenhouse",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00695C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Soil fertility pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SolarpunkEmerald.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Soil ${(plot.soilFertility * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarpunkEmerald,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Empty vs Growing vs Ripe
            if (plot.cropType == null) {
                // Empty Plot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟫", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fallow Loam Field",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!plot.hasGreenhouse && userCoins >= 220) {
                            OutlinedButton(
                                onClick = onUpgradeGreenhouse,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("☀️ Build Greenhouse (220c)", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = onPlantClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_plant_${plot.id}")
                        ) {
                            Text("🌱 Plant Seeds", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Planted Plot
                val crop = plot.cropType
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(crop.emoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = crop.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isRipe) "✨ Ripe & Ready to Harvest!" else "Growth: ${(plot.growthProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = if (isRipe) FontWeight.Bold else FontWeight.Normal,
                                color = if (isRipe) SolarpunkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isRipe) {
                        Button(
                            onClick = onHarvest,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_harvest_${plot.id}")
                        ) {
                            Text("Harvest", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (plot.waterLevel < 0.8f) {
                                Button(
                                    onClick = onWater,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("💧 Water", fontSize = 11.sp)
                                }
                            }
                            if (canEnrich && plot.soilFertility < 1.6f) {
                                OutlinedButton(
                                    onClick = onFertilize,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🧪 Bio-Enrich", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bars: Growth & Moisture
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Growth Progress", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(plot.growthProgress * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { plot.growthProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = SolarpunkEmerald,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Soil Moisture", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(plot.waterLevel * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { plot.waterLevel },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF0288D1),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlantSeedDialog(
    inventory: List<InventoryEntity>,
    onDismiss: () -> Unit,
    onSelectCrop: (CropType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Crop Seed", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose from available seeds in your barn:", fontSize = 12.sp)

                CropType.values().forEach { crop ->
                    val seedStock = inventory.find { it.itemId == crop.seedItem }?.quantity ?: 0
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableWithRipple {
                                if (seedStock > 0) onSelectCrop(crop)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(crop.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(crop.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Harvest: ${crop.harvestItem.displayName} • ${crop.growthHours}h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "$seedStock seeds",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (seedStock > 0) SolarpunkEmerald else Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
