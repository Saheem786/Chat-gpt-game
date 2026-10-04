package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
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
import com.example.data.local.InventoryEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.CraftingRecipe
import com.example.data.model.WorkshopRecipes
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun WorkshopScreen(
    state: FarmStateEntity? = null,
    workshopQueue: List<WorkshopQueueEntity>,
    inventory: List<InventoryEntity>,
    onStartCrafting: (CraftingRecipe) -> Unit,
    onCollectTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableEnergy = state?.batteryStored ?: 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("workshop_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workshop Overview Header
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
                                text = "Artisan Processing Hub",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Clean solar-powered transformation of raw materials",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("🏭", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stored Clean Battery Power:", fontSize = 11.sp)
                            }
                            Text(
                                text = "${availableEnergy.toInt()} / ${state?.batteryMax?.toInt() ?: 50} kWh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (availableEnergy > 5f) SolarpunkEmerald else Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }

        // Active Queue Section
        if (workshopQueue.isNotEmpty()) {
            item {
                Text(
                    text = "Active Processing Queue (${workshopQueue.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(workshopQueue) { task ->
                val recipe = WorkshopRecipes.ALL.find { it.id == task.recipeId }
                if (recipe != null) {
                    ActiveWorkshopCard(
                        task = task,
                        recipe = recipe,
                        onCollect = { onCollectTask(task.id) }
                    )
                }
            }
        }

        // Available Recipes Catalog
        item {
            Text(
                text = "Processing Facilities & Recipes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(WorkshopRecipes.ALL) { recipe ->
            val primaryStock = inventory.find { it.itemId == recipe.inputItem }?.quantity ?: 0
            val secStock = if (recipe.secondaryInput != null) {
                inventory.find { it.itemId == recipe.secondaryInput }?.quantity ?: 0
            } else 0

            val hasIngredients = primaryStock >= recipe.inputQuantity &&
                    (recipe.secondaryInput == null || secStock >= recipe.secondaryQuantity)
            val hasEnergy = availableEnergy >= recipe.energyCost
            val canCraft = hasIngredients && hasEnergy

            RecipeCraftCard(
                recipe = recipe,
                primaryStock = primaryStock,
                secondaryStock = secStock,
                hasEnergy = hasEnergy,
                canCraft = canCraft,
                onCraft = { onStartCrafting(recipe) }
            )
        }
    }
}

@Composable
fun ActiveWorkshopCard(
    task: WorkshopQueueEntity,
    recipe: CraftingRecipe,
    onCollect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workshop_task_${task.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(recipe.outputItem.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${recipe.building} • Producing ${recipe.outputQuantity}x ${recipe.outputItem.displayName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (task.isFinished) {
                    Button(
                        onClick = onCollect,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_collect_task_${task.id}")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect", fontSize = 11.sp)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SolarSunAmber.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = SolarSunAmber, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.progressHours.toInt()}/${task.totalHours}h",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolarSunAmber
                            )
                        }
                    }
                }
            }

            if (!task.isFinished) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (task.progressHours / task.totalHours).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SolarpunkEmerald,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecipeCraftCard(
    recipe: CraftingRecipe,
    primaryStock: Int,
    secondaryStock: Int,
    hasEnergy: Boolean,
    canCraft: Boolean,
    onCraft: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Text(recipe.outputItem.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${recipe.building} • Produces ${recipe.outputQuantity}x ${recipe.outputItem.displayName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SolarpunkEmerald.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Value: ${recipe.outputItem.basePrice * recipe.outputQuantity}c",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarpunkEmerald,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ingredients & Energy Requirements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${recipe.inputItem.iconEmoji} ${recipe.inputQuantity}x ${recipe.inputItem.displayName} ($primaryStock in stock)",
                            fontSize = 11.sp,
                            color = if (primaryStock >= recipe.inputQuantity) MaterialTheme.colorScheme.onSurface else Color(0xFFD32F2F)
                        )
                    }

                    if (recipe.secondaryInput != null) {
                        Text(
                            text = "+ ${recipe.secondaryInput.iconEmoji} ${recipe.secondaryQuantity}x ${recipe.secondaryInput.displayName} ($secondaryStock in stock)",
                            fontSize = 11.sp,
                            color = if (secondaryStock >= recipe.secondaryQuantity) MaterialTheme.colorScheme.onSurface else Color(0xFFD32F2F)
                        )
                    }

                    if (recipe.energyCost > 0) {
                        Text(
                            text = "⚡ Requires ${recipe.energyCost.toInt()} kWh battery power",
                            fontSize = 10.sp,
                            color = if (hasEnergy) SolarpunkEmerald else Color(0xFFD32F2F)
                        )
                    }
                }

                Button(
                    onClick = onCraft,
                    enabled = canCraft,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarpunkEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_craft_${recipe.id}")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start (${recipe.durationHours}h)", fontSize = 11.sp)
                }
            }
        }
    }
}
