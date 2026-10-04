package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.SolarGold40
import com.example.ui.theme.SolarSunAmber
import com.example.ui.theme.SolarpunkEmerald

@Composable
fun FarmTopHud(
    state: FarmStateEntity?,
    gameSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_hud")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Row 1: Coins, Eco Score, Day & Weather
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SolarSunAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = SolarGold40,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.coins}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Eco-Harmony Score
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SolarpunkEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forest,
                        contentDescription = "Eco Harmony",
                        tint = SolarpunkEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eco ${state.ecoHarmonyScore}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SolarpunkEmerald
                    )
                }

                // Day, Season & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${state.season.emoji} Day ${state.day} • ${state.hour}:00",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Weather Pill, Battery, Water & Simulation Speed Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weather Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = state.weather.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.weather.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Microgrid Quick Status: Battery & Water
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Energy",
                        tint = SolarSunAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${state.batteryStored.toInt()}/${state.batteryMax.toInt()}kwh",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${state.waterStored.toInt()}/${state.waterMax.toInt()}L",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Speed buttons: Pause, 1x, 2x, 5x
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { onSpeedChange(if (gameSpeed == 0f) 1f else 0f) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("speed_pause_play")
                    ) {
                        Icon(
                            imageVector = if (gameSpeed == 0f) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause or Play",
                            tint = if (gameSpeed == 0f) SolarSunAmber else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    listOf(1f, 2f, 5f).forEach { speed ->
                        val isSelected = gameSpeed == speed
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .size(24.dp)
                                .clickableWithRipple { onSpeedChange(speed) }
                        ) {
                            Text(
                                text = "${speed.toInt()}x",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier = this.clickable {
    onClick()
}
