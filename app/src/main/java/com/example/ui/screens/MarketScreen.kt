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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Water
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
import com.example.data.local.InventoryEntity
import com.example.data.local.MarketQuoteEntity
import com.example.data.model.ItemId
import com.example.data.model.MarketDemand
import com.example.data.model.Season
import com.example.data.model.WeatherType
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun MarketScreen(
    state: FarmStateEntity?,
    inventory: List<InventoryEntity>,
    marketQuotes: List<MarketQuoteEntity> = emptyList(),
    onWholesaleSell: (ItemId, Int) -> Unit,
    onGoFishing: () -> Unit,
    onActivateAquaponics: () -> Unit,
    onBuySeeds: (ItemId, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    val weather = state.weather
    val season = state.season

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("market_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sustainable Fishery Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐟", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sustainable Fishery & River Ecosystem",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Overfishing depletes native fish! Maintain ecological balance.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // River Health Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("River Wild Population Health", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${(state.fishPopulationHealth * 100).toInt()}% (${if (state.fishPopulationHealth > 0.6f) "Thriving" else "Depleted"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.fishPopulationHealth > 0.6f) SolarpunkEmerald else Color(0xFFD32F2F)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.fishPopulationHealth },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (state.fishPopulationHealth > 0.6f) Color(0xFF00897B) else Color(0xFFD32F2F),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onGoFishing,
                            enabled = state.fishPopulationHealth > 0.3f,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_fish_sustainable")
                        ) {
                            Text("🎣 Sustainable Cast", fontSize = 11.sp)
                        }

                        if (!state.aquaponicsActive) {
                            Button(
                                onClick = onActivateAquaponics,
                                enabled = state.coins >= 480,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("btn_aquaponics")
                            ) {
                                Text("☀️ Build Aquaponics (480c)", fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SolarpunkEmerald.copy(alpha = 0.2f),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(
                                    text = "✅ Solar Aquaponics Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolarpunkEmerald,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Authoritative Market Trends Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = SolarSunAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Authoritative Market Rates",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val trendAlert = when {
                        weather == WeatherType.DROUGHT -> "🏜️ DROUGHT IN EFFECT: Crop scarcity surged Tomato, Carrot, Corn & Wheat market prices +65%!"
                        weather == WeatherType.HEATWAVE -> "🔥 HEATWAVE: Town thirst in surge. Cold-Pressed Juices, Berries and Mint in sky-high demand!"
                        season == Season.WINTER -> "❄️ WINTER FREEZE: Wool, warm Artisan Blankets, and Hot Sourdough bread prices increased by +60%!"
                        season == Season.AUTUMN -> "🍂 AUTUMN HARVEST: Merchants are buying bulk Grains and Aged Cheeses with a +35% bonus!"
                        else -> "🌸 NORMAL SPRING COMMERCE: Stable trading prices across all agricultural and livestock sectors."
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = trendAlert,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (marketQuotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Active Market Quotes (Persistent for Day ${state.day}):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(marketQuotes) { quote ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(quote.itemId.iconEmoji, fontSize = 18.sp)
                                            Text(
                                                text = "${if (quote.priceChangePercent >= 0) "+" else ""}${quote.priceChangePercent}%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (quote.priceChangePercent >= 0) SolarpunkEmerald else Color(0xFFD32F2F)
                                            )
                                        }
                                        Text(quote.itemId.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("${quote.currentPrice}c (base: ${quote.basePrice}c)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(quote.demand.trendEmoji + " " + quote.demand.label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seed Merchant
        item {
            Text(
                text = "Seed Merchant Stall",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ItemId.SEED_WHEAT to "Wheat",
                    ItemId.SEED_CORN to "Corn",
                    ItemId.SEED_TOMATO to "Tomato",
                    ItemId.SEED_CARROT to "Carrot",
                    ItemId.SEED_STRAWBERRY to "Berry",
                    ItemId.SEED_HERB to "Herbs"
                ).forEach { (seedItem, name) ->
                    val canAfford = state.coins >= seedItem.basePrice * 3
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(seedItem.iconEmoji, fontSize = 18.sp)
                            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${seedItem.basePrice}c", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { onBuySeeds(seedItem, 3) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Text("3x", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // Wholesale Instant Liquidation
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Wholesale Caravan (Sell Any Stock in Bulk)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val sellableInventory = inventory.filter { it.quantity > 0 && it.itemId.category != com.example.data.model.ItemCategory.SEEDS }
        if (sellableInventory.isEmpty()) {
            item {
                Text(
                    text = "No sellable inventory in the barn right now. Harvest crops or collect animal produce first!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sellableInventory) { inv ->
                val quote = marketQuotes.find { it.itemId == inv.itemId }
                val unitPrice = quote?.currentPrice ?: inv.itemId.basePrice

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(inv.itemId.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(inv.itemId.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("In Barn: ${inv.quantity} • Wholesale price: ${unitPrice}c each", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onWholesaleSell(inv.itemId, 1) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_wholesale_sell_1_${inv.itemId.name.lowercase()}")
                            ) {
                                Text("Sell 1 (+${unitPrice}c)", fontSize = 11.sp)
                            }

                            if (inv.quantity >= 5) {
                                Button(
                                    onClick = { onWholesaleSell(inv.itemId, 5) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_wholesale_sell_5_${inv.itemId.name.lowercase()}")
                                ) {
                                    Text("Sell 5 (+${unitPrice * 5}c)", fontSize = 11.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
