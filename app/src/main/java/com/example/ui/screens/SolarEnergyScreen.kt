package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FarmStateEntity
import com.example.data.model.SettlementTier
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun SolarEnergyScreen(
    state: FarmStateEntity?,
    onBuySolarPanel: () -> Unit,
    onBuyWindTurbine: () -> Unit,
    onBuyRainCollector: () -> Unit,
    onUpgradeSettlementTier: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    val solarCost = 180 + (state.solarPanelsCount * 40)
    val windCost = 260 + (state.windTurbinesCount * 60)
    val rainCost = 120 + (state.rainCollectorsCount * 30)

    val nextTier = SettlementTier.values().find { it.level == state.settlementTier.level + 1 }
    val canUpgradeTier = nextTier != null && state.coins >= nextTier.requiredCoins && state.ecoHarmonyScore >= nextTier.requiredEcoScore

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("solar_energy_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Solarpunk Harmony Index Header Card
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
                        Column {
                            Text(
                                text = "Solarpunk Harmony Index",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Circular balance of clean power, soil regeneration, and zero-waste",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = SolarpunkEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${state.ecoHarmonyScore}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SolarpunkEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { state.ecoHarmonyScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SolarpunkEmerald,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Harmony Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HarmonyMetricPill(
                            label = "100% Clean Grid",
                            active = state.batteryStored > 10f,
                            icon = Icons.Default.Bolt
                        )
                        HarmonyMetricPill(
                            label = "Closed-Loop Compost",
                            active = true,
                            icon = Icons.Default.Recycling
                        )
                        HarmonyMetricPill(
                            label = "Certified Organic",
                            active = state.ecoHarmonyScore >= 50,
                            icon = Icons.Default.Forest
                        )
                    }
                }
            }
        }

        // Clean Energy Microgrid Telemetry
        item {
            Text(
                text = "Renewable Microgrid Telemetry",
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
                // Solar Grid Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SolarPower, contentDescription = null, tint = SolarSunAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Solar Grid", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${state.solarPanelsCount} Panel Arrays", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Output: ${(state.solarPanelsCount * 2.5f * state.weather.solarMultiplier).toInt()} kW/hr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Sun: ${state.weather.displayName}", fontSize = 10.sp, color = SolarSunAmber)
                    }
                }

                // Wind Turbine Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = Color(0xFF00897B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wind Farm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${state.windTurbinesCount} Micro Turbines", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Output: ${(state.windTurbinesCount * 1.8f * state.weather.windMultiplier).toInt()} kW/hr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Breeze: 24/7 Power", fontSize = 10.sp, color = Color(0xFF00897B))
                    }
                }

                // Rainwater Silo Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Water Silo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${state.rainCollectorsCount} Harvesters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${state.waterStored.toInt()}/${state.waterMax.toInt()} L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (state.weather.isRaining) "🌧️ Refilling!" else "Stored reserve", fontSize = 10.sp, color = Color(0xFF0288D1))
                    }
                }
            }
        }

        // Infrastructure Expansion Upgrades
        item {
            Text(
                text = "Clean Infrastructure Upgrades",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            UpgradeInfrastructureCard(
                title = "High-Efficiency Solar Array",
                subtitle = "+25 kWh Battery Storage & +2.5 kW generation",
                cost = solarCost,
                canAfford = state.coins >= solarCost,
                icon = Icons.Default.SolarPower,
                iconTint = SolarSunAmber,
                onBuy = onBuySolarPanel
            )
        }

        item {
            UpgradeInfrastructureCard(
                title = "Micro Wind Turbine Tower",
                subtitle = "+30 kWh Storage & continuous night-time power",
                cost = windCost,
                canAfford = state.coins >= windCost,
                icon = Icons.Default.Air,
                iconTint = Color(0xFF00897B),
                onBuy = onBuyWindTurbine
            )
        }

        item {
            UpgradeInfrastructureCard(
                title = "Rainwater Harvesting Silo",
                subtitle = "+50L water capacity for drought resilience",
                cost = rainCost,
                canAfford = state.coins >= rainCost,
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF0288D1),
                onBuy = onBuyRainCollector
            )
        }

        // Settlement Progression Advancement
        item {
            Text(
                text = "Settlement Milestone Progression",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = SolarSunAmber, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Current Tier: ${state.settlementTier.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(state.settlementTier.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (nextTier != null) {
                        Text(
                            text = "Next Tier: ${nextTier.title}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarpunkEmerald
                        )
                        Text(
                            text = "Perks: ${nextTier.perks}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Requirements: ${nextTier.requiredCoins} Coins (${state.coins}/${nextTier.requiredCoins}) & ${nextTier.requiredEcoScore}% Eco-Harmony (${state.ecoHarmonyScore}%/${nextTier.requiredEcoScore}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (canUpgradeTier) SolarpunkEmerald else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onUpgradeSettlementTier,
                            enabled = canUpgradeTier,
                            colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_upgrade_settlement_tier")
                        ) {
                            Text(if (canUpgradeTier) "🏆 Advance to ${nextTier.title}" else "Requirements not yet met")
                        }
                    } else {
                        Text(
                            text = "🌟 Congratulations! You have achieved the peak Solarpunk Eco-Enterprise tier. The valley flourishes under your circular green economy.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SolarpunkEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HarmonyMetricPill(label: String, active: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (active) SolarpunkEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) SolarpunkEmerald else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) SolarpunkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UpgradeInfrastructureCard(
    title: String,
    subtitle: String,
    cost: Int,
    canAfford: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("${cost}c", fontSize = 11.sp)
            }
        }
    }
}
