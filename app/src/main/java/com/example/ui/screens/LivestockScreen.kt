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
import com.example.ui.components.clickableWithRipple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.local.AnimalEntity
import com.example.data.local.FarmStateEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.LivestockValuation
import com.example.data.model.MeatProcessingYield
import com.example.ui.theme.SolarGold40
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun LivestockScreen(
    state: FarmStateEntity?,
    animals: List<AnimalEntity>,
    onFeedAnimals: () -> Unit,
    onWaterAnimals: () -> Unit,
    onCollectProduce: (Long) -> Unit,
    onCollectAll: () -> Unit,
    onPetAnimal: (Long) -> Unit,
    onBuyAnimal: (AnimalSpecies, String) -> Unit,
    onSellAnimal: (Long) -> Unit,
    onProcessAnimal: (Long) -> Unit,
    onBreedAnimal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdoptDialog by remember { mutableStateOf(false) }
    var animalToSell by remember { mutableStateOf<AnimalEntity?>(null) }
    var animalToProcess by remember { mutableStateOf<AnimalEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdoptDialog = true },
                containerColor = SolarpunkEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_adopt_animal")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adopt Animal")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adopt Animal", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("livestock_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info & Quick Actions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
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
                                    text = "Livestock Sanctuary",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${animals.size} animals • Ethically raised & solar sheltered",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("🐾", fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onFeedAnimals,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_feed_animals")
                            ) {
                                Text("🌾 Feed All", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onWaterAnimals,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_water_animals")
                            ) {
                                Text("💧 Water All", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onCollectAll,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("btn_collect_animals")
                            ) {
                                Text("🧺 Harvest All", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Ecological Loop Info Callout
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SolarpunkEmerald.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Circular Loop: Animals naturally generate Raw Manure when fed! Compost it in the Agriculture tab to create Organic Fertilizer & Biogas.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Animal List
            items(animals) { animal ->
                AnimalCard(
                    animal = animal,
                    onPet = { onPetAnimal(animal.id) },
                    onCollect = { onCollectProduce(animal.id) },
                    onBreed = { onBreedAnimal(animal.id) },
                    onSell = { animalToSell = animal },
                    onProcess = { animalToProcess = animal }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
            }
        }
    }

    if (showAdoptDialog) {
        AdoptAnimalDialog(
            userCoins = state?.coins ?: 0,
            onDismiss = { showAdoptDialog = false },
            onConfirm = { species, name ->
                onBuyAnimal(species, name)
                showAdoptDialog = false
            }
        )
    }

    // Confirmation Dialog for Animal Selling (Section 21)
    animalToSell?.let { animal ->
        val estValue = LivestockValuation.calculateSaleValue(animal.species, animal.ageDays, animal.health, animal.happiness)
        AlertDialog(
            onDismissRequest = { animalToSell = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sell ${animal.nickname}?", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("💰")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Species: ${animal.species.displayName} (${animal.species.emoji})")
                    Text("Age: ${animal.ageDays} days old")
                    Text("Health: ${(animal.health * 100).toInt()}% • Happiness: ${(animal.happiness * 100).toInt()}%")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SolarSunAmber.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Estimated Market Value: $estValue Coins",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SolarSunAmber,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "⚠️ Warning: Selling this animal permanently removes them from your livestock sanctuary.",
                        fontSize = 11.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSellAnimal(animal.id)
                        animalToSell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                    modifier = Modifier.testTag("btn_confirm_sell_${animal.id}")
                ) {
                    Text("Sell for $estValue Coins", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { animalToSell = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Animal Processing (Section 21)
    animalToProcess?.let { animal ->
        val yield = LivestockValuation.calculateMeatYield(animal.species, animal.nickname, animal.ageDays, animal.health)
        AlertDialog(
            onDismissRequest = { animalToProcess = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Process ${animal.nickname}?", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🥩")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!yield.isEligible) {
                        Text(
                            text = yield.rejectionReason ?: "This animal cannot be processed into meat.",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text("Species: ${animal.species.displayName} (${animal.species.emoji})")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Expected Output:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("• ${yield.meatCount}x Pasture Meat 🥩")
                                if (yield.hideCount > 0) {
                                    Text("• ${yield.hideCount}x Eco-Hide 👞")
                                }
                            }
                        }
                        Text(
                            text = "⚠️ Warning: Processing permanently removes this animal from your farm.",
                            fontSize = 11.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            },
            confirmButton = {
                if (yield.isEligible) {
                    Button(
                        onClick = {
                            onProcessAnimal(animal.id)
                            animalToProcess = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.testTag("btn_confirm_process_${animal.id}")
                    ) {
                        Text("Process into Meat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { animalToProcess = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AnimalCard(
    animal: AnimalEntity,
    onPet: () -> Unit,
    onCollect: () -> Unit,
    onBreed: () -> Unit,
    onSell: () -> Unit,
    onProcess: () -> Unit
) {
    val estValue = LivestockValuation.calculateSaleValue(animal.species, animal.ageDays, animal.health, animal.happiness)
    val isMature = animal.ageDays >= animal.species.breedingMaturityDays
    val canProcess = animal.species != AnimalSpecies.BEES

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("animal_card_${animal.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Emoji, Name, Nickname & Pet button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Text(animal.species.emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = animal.nickname,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = animal.species.displayName,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Shelter: ${animal.species.shelterName} • Age ${animal.ageDays}d (${if (isMature) "Mature" else "Young"})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Pet with love button
                IconButton(
                    onClick = onPet,
                    modifier = Modifier.testTag("btn_pet_${animal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Pet Animal",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Valuation & State Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SolarSunAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Value: $estValue Coins",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarSunAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                val statusText = when {
                    animal.isPregnant -> "💕 Pregnant (${animal.pregnancyHours}/${animal.species.gestationHours}h)"
                    animal.species.primaryProduce == null -> "No recurring goods (Process for meat/hide)"
                    animal.produceReady -> "Produce Ready! 🧺"
                    else -> "Produce in ${animal.hoursUntilProduce}h"
                }
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = if (animal.produceReady || animal.isPregnant) FontWeight.Bold else FontWeight.Normal,
                    color = if (animal.isPregnant) Color(0xFFE91E63) else if (animal.produceReady) SolarpunkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Biology Bars: Hunger, Thirst, Health, Happiness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hunger
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hunger", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (animal.hunger < 0.3f) "Fed" else "Hungry", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { animal.hunger },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (animal.hunger > 0.6f) Color(0xFFE53935) else SolarSunAmber,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Thirst
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Thirst", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (animal.thirst < 0.3f) "Quenched" else "Thirsty", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { animal.thirst },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (animal.thirst > 0.6f) Color(0xFFE53935) else Color(0xFF0288D1),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Happiness
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Happiness", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(animal.happiness * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { animal.happiness },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SolarpunkEmerald,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Collect, Breed, Sell, Process
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (animal.produceReady) {
                    Button(
                        onClick = onCollect,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_collect_${animal.id}")
                    ) {
                        Text("🧺 Collect", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                if (animal.species != AnimalSpecies.BEES) {
                    OutlinedButton(
                        onClick = onBreed,
                        enabled = isMature && !animal.isPregnant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_breed_${animal.id}")
                    ) {
                        Text(if (animal.isPregnant) "Pregnant" else "💕 Breed", fontSize = 10.sp)
                    }
                }

                OutlinedButton(
                    onClick = onSell,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_sell_${animal.id}")
                ) {
                    Text("💰 Sell", fontSize = 10.sp)
                }

                if (canProcess) {
                    OutlinedButton(
                        onClick = onProcess,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_process_${animal.id}")
                    ) {
                        Text("🥩 Meat", fontSize = 10.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdoptAnimalDialog(
    userCoins: Int,
    onDismiss: () -> Unit,
    onConfirm: (AnimalSpecies, String) -> Unit
) {
    var selectedSpecies by remember { mutableStateOf(AnimalSpecies.CHICKEN) }
    var nickname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Adopt Livestock", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🏡")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select an animal species to raise on your solar farm:", fontSize = 12.sp)

                // Species Options
                AnimalSpecies.values().forEach { species ->
                    val isSelected = selectedSpecies == species
                    val canAfford = userCoins >= species.purchaseCost
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SolarpunkEmerald) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableWithRipple { selectedSpecies = species }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(species.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(species.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Produces: ${species.primaryProduce?.displayName ?: "Meat & Hide (processed)"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${species.purchaseCost} Coins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (canAfford) SolarSunAmber else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Animal Nickname (Optional)") },
                    placeholder = { Text("e.g. Daisy, Barnaby...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedSpecies, nickname) },
                enabled = userCoins >= selectedSpecies.purchaseCost,
                colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald)
            ) {
                Text("Adopt (${selectedSpecies.purchaseCost} Coins)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
