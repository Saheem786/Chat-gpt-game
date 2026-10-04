package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.clickableWithRipple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AnimalEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.LogMessageEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.SettlementTier
import com.example.ui.theme.SolarGold40
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun DashboardScreen(
    state: FarmStateEntity?,
    animals: List<AnimalEntity>,
    plots: List<CropPlotEntity>,
    workshops: List<WorkshopQueueEntity>,
    shelves: List<ShopShelfEntity>,
    logs: List<LogMessageEntity>,
    onFeedAnimals: () -> Unit,
    onWaterPlots: () -> Unit,
    onCollectProduce: () -> Unit,
    onCompostManure: () -> Unit,
    onUpgradeSettlement: () -> Unit,
    onNavigateTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    val readyAnimalProduce = animals.count { it.produceReady }
    val ripeCrops = plots.count { it.isReadyForHarvest || it.growthProgress >= 1f }
    val readyWorkshopTasks = workshops.count { it.isFinished }
    val nextTier = SettlementTier.values().find { it.level == state.settlementTier.level + 1 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_solarpunk_hero),
                        contentDescription = "Solarpunk Settlement Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC0C2415)),
                                    startY = 60f
                                )
                            )
                    )
                    // Hero Text
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SOLARPUNK ECO-FARM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarSunAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = state.settlementTier.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Circular Economy • Renewable Power • Zero Waste",
                            fontSize = 12.sp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }

        // Quick Operational Actions
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
                        Text(
                            text = "Quick Operations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (readyAnimalProduce + ripeCrops + readyWorkshopTasks > 0) {
                            Text(
                                text = "✨ Ready items available!",
                                fontSize = 12.sp,
                                color = SolarpunkEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onFeedAnimals,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_feed_all")
                        ) {
                            Text("🌾 Feed All", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onWaterPlots,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_water_all")
                        ) {
                            Text("💧 Irrigate", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onCollectProduce,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("btn_collect_all")
                        ) {
                            Text("🧺 Collect ($readyAnimalProduce)", fontSize = 12.sp, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Composting Waste-To-Resource button
                    OutlinedButton(
                        onClick = onCompostManure,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_compost_loop")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = "Compost",
                            tint = SolarpunkEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zero-Waste Loop: Compost 3 Manure → 2 Rich Compost + 1 Biogas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Live Ecosystem Status Grid
        item {
            Text(
                text = "Settlement Divisions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Livestock Division Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickableWithRipple { onNavigateTab(1) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐄", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Livestock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${animals.size} Animals sanctuary",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (readyAnimalProduce > 0) "$readyAnimalProduce ready to collect!" else "Grazing peacefully",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (readyAnimalProduce > 0) SolarpunkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Agriculture Division Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickableWithRipple { onNavigateTab(2) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agriculture", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${plots.count { it.cropType != null }}/${plots.size} Plots planted",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (ripeCrops > 0) "$ripeCrops crops ripe!" else "Growing in sun",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ripeCrops > 0) SolarpunkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Workshops Division Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickableWithRipple { onNavigateTab(3) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏭", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Workshops", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${workshops.size} Active queues",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Solar Bakery & Dairy",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Retail Eco-Shop Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickableWithRipple { onNavigateTab(4) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏪", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retail Store", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val stockedCount = shelves.count { it.stockedItemId != null && it.quantity > 0 }
                        Text(
                            text = "$stockedCount/6 Shelves stocked",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "NPC Customers visiting",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SolarpunkEmerald
                        )
                    }
                }
            }
        }

        // Settlement Tier Milestone Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Settlement Tier: ${state.settlementTier.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.settlementTier.perks,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Tier",
                            tint = SolarSunAmber,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    if (nextTier != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Next: ${nextTier.title} (Requires ${nextTier.requiredCoins} Coins & ${nextTier.requiredEcoScore}% Eco)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val canUpgrade = state.coins >= nextTier.requiredCoins && state.ecoHarmonyScore >= nextTier.requiredEcoScore
                        Button(
                            onClick = onUpgradeSettlement,
                            enabled = canUpgrade,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_upgrade_tier")
                        ) {
                            Text(if (canUpgrade) "🏆 Upgrade to ${nextTier.title}" else "🔒 Meet requirements to expand")
                        }
                    }
                }
            }
        }

        // Live Settlement Log
        item {
            Text(
                text = "Settlement Activity Log",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = "No recent events recorded.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(logs.take(8)) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (log.category) {
                            "ANIMALS" -> "🐾"
                            "CROPS" -> "🌿"
                            "ECOLOGY" -> "♻️"
                            "SHOP" -> "🏪"
                            "WORKSHOP" -> "⚙️"
                            "FISHERY" -> "🐟"
                            "WEATHER" -> "⛅"
                            else -> "🏛️"
                        }
                        Text(text = icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Day ${log.day}, ${log.hour}:00",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
