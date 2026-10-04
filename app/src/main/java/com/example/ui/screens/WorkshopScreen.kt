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
import com.example.data.local.InventoryEntity
import com.example.data.local.WorkshopQueueEntity
import com.example.data.model.CraftingRecipe
import com.example.data.model.WorkshopRecipes
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun WorkshopScreen(
    workshopQueue: List<WorkshopQueueEntity>,
    inventory: List<InventoryEntity>,
    onStartCrafting: (CraftingRecipe) -> Unit,
    onCollectTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
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
                                text = "Transform raw farm products into high-value artisan crafts",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("🏭", fontSize = 28.sp)
                    }
                }
            }
        }

        // Active Queues Section
        if (workshopQueue.isNotEmpty()) {
            item {
                Text(
                    text = "Active Production Queues (${workshopQueue.size})",
                    fontSize = 15.sp,
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

            val canCraft = primaryStock >= recipe.inputQuantity &&
                    (recipe.secondaryInput == null || secStock >= recipe.secondaryQuantity)

            RecipeCraftCard(
                recipe = recipe,
                primaryStock = primaryStock,
                secondaryStock = secStock,
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
                        Text(recipe.building, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (task.isFinished) {
                    Button(
                        onClick = onCollect,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarSunAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_collect_task_${task.id}")
                    ) {
                        Text("Collect", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${task.totalHours - task.progressHours.toInt()}h left", fontSize = 10.sp)
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

            // Ingredients Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Requires: ${recipe.inputQuantity}x ${recipe.inputItem.displayName} ($primaryStock in stock)",
                        fontSize = 11.sp,
                        color = if (primaryStock >= recipe.inputQuantity) MaterialTheme.colorScheme.onSurface else Color(0xFFD32F2F),
                        fontWeight = if (primaryStock >= recipe.inputQuantity) FontWeight.Normal else FontWeight.SemiBold
                    )
                    if (recipe.secondaryInput != null) {
                        Text(
                            text = "+ ${recipe.secondaryQuantity}x ${recipe.secondaryInput.displayName} ($secondaryStock in stock)",
                            fontSize = 11.sp,
                            color = if (secondaryStock >= recipe.secondaryQuantity) MaterialTheme.colorScheme.onSurface else Color(0xFFD32F2F)
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
                    Text("Start (${recipe.durationHours}h)", fontSize = 11.sp)
                }
            }
        }
    }
}
