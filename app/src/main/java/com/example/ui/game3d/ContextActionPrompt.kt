package com.example.ui.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game3d.data.InteractableTarget
import com.example.game3d.data.InteractableType
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun ContextActionPrompt(
    target: InteractableTarget?,
    modifier: Modifier = Modifier,
    onActionClicked: (InteractableTarget) -> Unit
) {
    AnimatedVisibility(
        visible = target != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        if (target != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xEE122019)
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(SolarpunkEmerald, SolarSunAmber)),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Icon & Target Information
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(SolarpunkEmerald.copy(alpha = 0.35f), Color(0x33000000))
                                    ),
                                    shape = CircleShape
                                )
                                .border(1.dp, SolarpunkEmerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (target.type) {
                                InteractableType.CROP_PLOT -> Icons.Default.Grass
                                InteractableType.ANIMAL -> Icons.Default.Pets
                                InteractableType.WORKSHOP -> Icons.Default.Build
                                InteractableType.ECO_SHOP -> Icons.Default.Storefront
                                InteractableType.MARKET -> Icons.Default.ShoppingBag
                                InteractableType.SOLAR_STATION -> Icons.Default.SolarPower
                                InteractableType.COMPOSTER -> Icons.Default.Recycling
                                InteractableType.FARM_HOUSE -> Icons.Default.Home
                                InteractableType.NPC -> Icons.Default.Pets
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = target.title,
                                tint = SolarSunAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = target.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = target.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1FAE5),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Primary Action Button
                    Button(
                        onClick = { onActionClicked(target) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarpunkEmerald,
                            contentColor = Color(0xFF064E3B)
                        ),
                        modifier = Modifier.testTag("context_action_button")
                    ) {
                        Text(
                            text = target.primaryAction,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
