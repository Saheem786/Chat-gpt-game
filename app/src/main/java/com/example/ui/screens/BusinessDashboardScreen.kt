package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.AnimalEntity
import com.example.data.local.ContractEntity
import com.example.data.local.CropPlotEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.BusinessLevel
import com.example.data.model.ItemId
import com.example.data.model.MarketDemand
import com.example.data.model.MarketItemQuote
import com.example.ui.theme.SolarGold40
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun BusinessDashboardScreen(
    state: FarmStateEntity?,
    animals: List<AnimalEntity>,
    plots: List<CropPlotEntity>,
    inventory: List<InventoryEntity>,
    workshops: List<WorkshopQueueEntity>,
    contracts: List<ContractEntity>,
    marketQuotes: List<MarketItemQuote>,
    onFulfillContract: (String) -> Unit,
    onAdvanceBusinessLevel: () -> Unit,
    onWholesaleSell: (ItemId, Int) -> Unit,
    onBuySolarPanel: () -> Unit,
    onBuyWindTurbine: () -> Unit,
    onBuyRainCollector: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    var selectedSubSection by remember { mutableIntStateOf(0) }

    val netProfit = state.totalEarnings - state.totalExpenses
    val totalInventoryValue = inventory.sumOf { (it.quantity * it.itemId.basePrice).toLong() }
    val nextLevel = BusinessLevel.values().find { it.level == state.businessLevel.level + 1 }
    val canAdvance = nextLevel != null &&
            state.totalEarnings >= nextLevel.requiredTotalRevenue &&
            state.businessReputation >= nextLevel.requiredReputation

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("business_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Level & Brand Header
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
                                text = "Solaris Eco-Enterprise",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Business Level ${state.businessLevel.level}: ${state.businessLevel.title}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolarpunkEmerald
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = SolarpunkEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "★ ${state.businessReputation}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolarpunkEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.businessLevel.perkDescription,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (nextLevel != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val revenueProgress = (state.totalEarnings.toFloat() / nextLevel.requiredTotalRevenue).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Next: ${nextLevel.title}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "${state.totalEarnings}/${nextLevel.requiredTotalRevenue}c • ${state.businessReputation}/${nextLevel.requiredReputation} Rep",
                                fontSize = 11.sp,
                                color = if (canAdvance) SolarpunkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { revenueProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SolarpunkEmerald,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        if (canAdvance) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onAdvanceBusinessLevel,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_advance_business_level")
                            ) {
                                Text("🏆 Advance to ${nextLevel.title}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Financial Ledger Summary Matrix
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Revenue
                BusinessMiniMetricCard(
                    title = "Revenue",
                    value = "${state.totalEarnings}c",
                    subtext = "Today: +${state.salesToday}c",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    tint = SolarpunkEmerald,
                    modifier = Modifier.weight(1f)
                )

                // Expenses
                BusinessMiniMetricCard(
                    title = "Expenses",
                    value = "${state.totalExpenses}c",
                    subtext = "Investments",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )

                // Net Profit
                BusinessMiniMetricCard(
                    title = "Net Profit",
                    value = "${netProfit}c",
                    subtext = if (netProfit >= 0) "Profitable" else "Deficit",
                    icon = Icons.Default.AccountBalance,
                    tint = if (netProfit >= 0) SolarSunAmber else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Inventory Value
                BusinessMiniMetricCard(
                    title = "Barn Assets",
                    value = "${totalInventoryValue}c",
                    subtext = "${inventory.sumOf { it.quantity }} items",
                    icon = Icons.Default.Inventory2,
                    tint = Color(0xFF00897B),
                    modifier = Modifier.weight(1f)
                )

                // Wholesale Income
                BusinessMiniMetricCard(
                    title = "Wholesale",
                    value = "${state.wholesaleIncomeTotal}c",
                    subtext = "B2B Contracts",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )

                // Livestock Assets
                BusinessMiniMetricCard(
                    title = "Livestock",
                    value = "${animals.size}",
                    subtext = "${state.livestockSoldTotal} sold, ${state.meatProcessedTotal} proc",
                    icon = Icons.Default.Pets,
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sub-Navigation Tabs: Overview, Wholesale, Market Tickers, Microgrid
        item {
            TabRow(
                selectedTabIndex = selectedSubSection,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SolarpunkEmerald,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSubSection == 0,
                    onClick = { selectedSubSection = 0 },
                    text = { Text("📜 Wholesale", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubSection == 1,
                    onClick = { selectedSubSection = 1 },
                    text = { Text("📈 Market", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubSection == 2,
                    onClick = { selectedSubSection = 2 },
                    text = { Text("📊 Overview", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubSection == 3,
                    onClick = { selectedSubSection = 3 },
                    text = { Text("⚡ Microgrid", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Section 0: Wholesale Contracts
        if (selectedSubSection == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Corporate Wholesale Orders (${contracts.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "B2B Commercial Supply",
                        fontSize = 11.sp,
                        color = SolarpunkEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(contracts) { contract ->
                val stock = inventory.find { it.itemId == contract.requestedItem }?.quantity ?: 0
                val canDeliver = stock >= contract.requestedQuantity && !contract.isCompleted

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (contract.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contract.clientAvatarEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(contract.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(contract.clientRole, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SolarSunAmber.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+${contract.rewardCoins}c",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SolarSunAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SolarpunkEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "+${contract.rewardEcoScore} Rep",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = SolarpunkEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order: ${contract.requestedQuantity}x ${contract.requestedItem.displayName} ($stock in barn) • Due: Day ${contract.expiryDay}",
                                fontSize = 11.sp,
                                color = if (stock >= contract.requestedQuantity) MaterialTheme.colorScheme.onSurface else Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
                            )

                            if (contract.isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SolarpunkEmerald.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SolarpunkEmerald, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delivered", fontSize = 10.sp, color = SolarpunkEmerald, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onFulfillContract(contract.id) },
                                    enabled = canDeliver,
                                    colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Deliver", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Dynamic Market Price Tickers
        if (selectedSubSection == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dynamic Market Price Quotes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Supply & Demand",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(marketQuotes) { quote ->
                val stock = inventory.find { it.itemId == quote.itemId }?.quantity ?: 0
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(quote.itemId.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(quote.itemId.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "${quote.marketDriver} • Barn: $stock",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${quote.currentPrice}c",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (quote.priceChangePercent > 0) SolarpunkEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (quote.priceChangePercent >= 0) "+" else ""}${quote.priceChangePercent}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (quote.priceChangePercent > 0) SolarpunkEmerald else Color(0xFFE53935)
                                )
                            }

                            if (stock > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onWholesaleSell(quote.itemId, 1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Sell", fontSize = 10.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Overview & Detailed Statistics
        if (selectedSubSection == 2) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Business Statistics & Asset Valuation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        StatisticRow(label = "Total Enterprise Revenue", value = "${state.totalEarnings} Coins")
                        StatisticRow(label = "Total Operating Expenses", value = "${state.totalExpenses} Coins")
                        StatisticRow(label = "Net Circular Profit", value = "${netProfit} Coins")
                        StatisticRow(label = "Wholesale B2B Earnings", value = "${state.wholesaleIncomeTotal} Coins")
                        StatisticRow(label = "Total Livestock Traded", value = "${state.livestockSoldTotal} Animals")
                        StatisticRow(label = "Pasture Meat Processed", value = "${state.meatProcessedTotal} Livestock")
                        StatisticRow(label = "Barn Warehoused Goods Value", value = "${totalInventoryValue} Coins")
                        StatisticRow(label = "Solar Clean Energy Coverage", value = "${state.solarPanelsCount * 25 + state.windTurbinesCount * 30} kWh Storage")
                        StatisticRow(label = "Water Silo Capacity", value = "${state.waterMax.toInt()} Liters")
                    }
                }
            }
        }

        // Section 3: Microgrid & Energy
        if (selectedSubSection == 3) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Clean Power & Water Infrastructure", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Renewable power keeps workshop production and farm automation operating with zero pollution.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("☀️ Solar Array (${state.solarPanelsCount})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("+25kWh battery capacity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val solarCost = 180 + (state.solarPanelsCount * 40)
                            Button(
                                onClick = onBuySolarPanel,
                                enabled = state.coins >= solarCost,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("${solarCost}c", fontSize = 11.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("💨 Wind Turbine (${state.windTurbinesCount})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Continuous night & day breeze power", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val windCost = 260 + (state.windTurbinesCount * 60)
                            Button(
                                onClick = onBuyWindTurbine,
                                enabled = state.coins >= windCost,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("${windCost}c", fontSize = 11.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("🌧️ Rain Silo (${state.rainCollectorsCount})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("+50L water storage reserve", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val rainCost = 120 + (state.rainCollectorsCount * 30)
                            Button(
                                onClick = onBuyRainCollector,
                                enabled = state.coins >= rainCost,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("${rainCost}c", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessMiniMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtext, fontSize = 9.sp, color = tint, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
