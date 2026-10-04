package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.AnimalEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.LivestockValuation
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun AnimalDetailModal(
    animal: AnimalEntity,
    onDismiss: () -> Unit,
    onFeed: () -> Unit,
    onWater: () -> Unit,
    onCollectProduce: () -> Unit,
    onBreed: () -> Unit,
    onSell: (Long) -> Unit,
    onProcess: (Long) -> Unit
) {
    var showSellConfirm by remember { mutableStateOf(false) }
    var showProcessConfirm by remember { mutableStateOf(false) }

    val estimatedPrice = remember(animal) {
        LivestockValuation.calculateSaleValue(animal.species, animal.ageDays, animal.health, animal.happiness)
    }

    val meatYield = remember(animal) {
        LivestockValuation.calculateMeatYield(animal.species, animal.nickname, animal.ageDays, animal.health)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = animal.species.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = animal.nickname,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${animal.species.displayName} • Age ${animal.ageDays}d",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Bars
                Text(text = "Health: ${(animal.health * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(
                    progress = { animal.health },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = SolarpunkEmerald
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Happiness: ${(animal.happiness * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(
                    progress = { animal.happiness },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = SolarSunAmber
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Chips
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (animal.produceReady) {
                        Box(
                            modifier = Modifier
                                .background(SolarSunAmber.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🎁 Produce Ready!", fontSize = 11.sp, color = SolarSunAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (animal.isPregnant) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF472B6).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🤰 Pregnant (${animal.pregnancyHours}/${animal.species.gestationHours}h)", fontSize = 11.sp, color = Color(0xFFF472B6), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Care Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onFeed,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Feed", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onWater,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Water", fontSize = 12.sp)
                    }

                    if (animal.produceReady) {
                        Button(
                            onClick = onCollectProduce,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Collect", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Breeding, Sell, Process Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBreed,
                        modifier = Modifier.weight(1f),
                        enabled = !animal.isPregnant && animal.ageDays >= animal.species.breedingMaturityDays
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Breed", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showSellConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = null, tint = SolarSunAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sell", fontSize = 12.sp)
                    }

                    if (animal.species != AnimalSpecies.BEES) {
                        OutlinedButton(
                            onClick = { showProcessConfirm = true },
                            modifier = Modifier.weight(1f),
                            enabled = meatYield.isEligible
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Process", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Sell Confirmation Dialog
    if (showSellConfirm) {
        AlertDialog(
            onDismissRequest = { showSellConfirm = false },
            title = { Text("Sell ${animal.nickname}?") },
            text = {
                Text(
                    "Sell ${animal.nickname} (${animal.species.displayName}) to the settlement livestock trade for $estimatedPrice Coins?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSellConfirm = false
                        onSell(animal.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber)
                ) {
                    Text("Confirm Sell ($estimatedPrice c)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Meat Processing Confirmation Dialog
    if (showProcessConfirm) {
        AlertDialog(
            onDismissRequest = { showProcessConfirm = false },
            title = { Text("Process ${animal.nickname} for Meat?") },
            text = {
                Text(
                    "Ethically harvest ${animal.nickname}.\n\nExpected Yield:\n• ${meatYield.meatCount}x Meat\n" +
                    if (meatYield.hideCount > 0) "• ${meatYield.hideCount}x Eco-Leather Hide\n" else ""
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProcessConfirm = false
                        onProcess(animal.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Processing")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProcessConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
