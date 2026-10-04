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
import androidx.compose.foundation.lazy.items
import com.example.ui.components.clickableWithRipple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ContractEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.ShopShelfEntity
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun EcoShopScreen(
    state: FarmStateEntity?,
    shelves: List<ShopShelfEntity>,
    contracts: List<ContractEntity>,
    inventory: List<InventoryEntity>,
    onStockShelf: (Int, ItemId, Int) -> Unit,
    onClearShelf: (Int) -> Unit,
    onUpdatePricing: (Int, PricingStrategy) -> Unit,
    onFulfillContract: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var stockingShelfId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ecoshop_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storefront Banner
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
                                text = "Settlement Eco-Boutique",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Retail storefront: NPC shoppers visit and buy directly from your shelves",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("🏪", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Total Career Sales: ${state?.totalEarnings ?: 0}c",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolarSunAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Reputation: ${state?.ecoHarmonyScore ?: 50}% Eco",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolarpunkEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Retail Display Shelves
        item {
            Text(
                text = "Retail Display Shelves (${shelves.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(shelves) { shelf ->
            ShelfCard(
                shelf = shelf,
                onStockClick = { stockingShelfId = shelf.shelfId },
                onClearClick = { onClearShelf(shelf.shelfId) },
                onStrategyChange = { strategy -> onUpdatePricing(shelf.shelfId, strategy) }
            )
        }

        // NPC Settlement Delivery Contracts
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NPC Delivery Contracts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "High Payout Bounties",
                    fontSize = 12.sp,
                    color = SolarpunkEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        items(contracts) { contract ->
            val playerStock = inventory.find { it.itemId == contract.requestedItem }?.quantity ?: 0
            val canFulfill = playerStock >= contract.requestedQuantity && !contract.isCompleted

            ContractCard(
                contract = contract,
                playerStock = playerStock,
                canFulfill = canFulfill,
                onFulfill = { onFulfillContract(contract.id) }
            )
        }
    }

    stockingShelfId?.let { shelfId ->
        StockShelfDialog(
            inventory = inventory.filter { it.quantity > 0 },
            onDismiss = { stockingShelfId = null },
            onSelect = { itemId, qty ->
                onStockShelf(shelfId, itemId, qty)
                stockingShelfId = null
            }
        )
    }
}

@Composable
fun ShelfCard(
    shelf: ShopShelfEntity,
    onStockClick: () -> Unit,
    onClearClick: () -> Unit,
    onStrategyChange: (PricingStrategy) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shelf_card_${shelf.shelfId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Shelf # and Clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shelf #${shelf.shelfId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (shelf.stockedItemId != null && shelf.quantity > 0) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Shelf", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (shelf.stockedItemId == null || shelf.quantity <= 0) {
                // Empty Shelf
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Empty shelf slot. Stock products to attract shoppers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onStockClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_stock_shelf_${shelf.shelfId}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stock Shelf", fontSize = 11.sp)
                    }
                }
            } else {
                // Stocked Shelf
                val item = shelf.stockedItemId
                val unitPrice = (item.basePrice * shelf.pricingStrategy.priceMultiplier).toInt().coerceAtLeast(1)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.iconEmoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(item.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "Stock: ${shelf.quantity} units • Price: ${unitPrice}c / unit",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onStockClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Add More", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pricing Strategy Selector
                Text(
                    text = "Pricing Strategy:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PricingStrategy.values().forEach { strategy ->
                        val isSelected = shelf.pricingStrategy == strategy
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStrategyChange(strategy) },
                            label = {
                                Text(
                                    text = strategy.label.substringBefore(" ("),
                                    fontSize = 9.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolarpunkEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = SolarpunkEmerald
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContractCard(
    contract: ContractEntity,
    playerStock: Int,
    canFulfill: Boolean,
    onFulfill: () -> Unit
) {
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
                    Text(contract.clientAvatarEmoji, fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(contract.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                            fontSize = 12.sp,
                            color = SolarSunAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SolarpunkEmerald.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${contract.rewardEcoScore} Eco",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SolarpunkEmerald,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Requested: ${contract.requestedQuantity}x ${contract.requestedItem.displayName} ($playerStock in barn)",
                    fontSize = 12.sp,
                    color = if (playerStock >= contract.requestedQuantity) MaterialTheme.colorScheme.onSurface else Color(0xFFD32F2F),
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
                            Text("Fulfilled", fontSize = 11.sp, color = SolarpunkEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onFulfill,
                        enabled = canFulfill,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_fulfill_${contract.id}")
                    ) {
                        Text("Deliver & Earn", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StockShelfDialog(
    inventory: List<InventoryEntity>,
    onDismiss: () -> Unit,
    onSelect: (ItemId, Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Shelf", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select item from inventory to put on display shelf:", fontSize = 12.sp)

                if (inventory.isEmpty()) {
                    Text("No items currently in inventory to stock.", color = Color.Gray)
                } else {
                    inventory.forEach { inv ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableWithRipple {
                                    val amount = inv.quantity.coerceAtMost(5)
                                    onSelect(inv.itemId, amount)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(inv.itemId.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(inv.itemId.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Base value: ${inv.itemId.basePrice}c", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text("${inv.quantity} in stock", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
