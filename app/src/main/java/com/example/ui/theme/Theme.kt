package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SolarpunkGreen80,
    onPrimary = SolarpunkDeepForest,
    primaryContainer = SolarpunkEmerald,
    onPrimaryContainer = Color(0xFFD7FFD9),
    secondary = SolarGold80,
    onSecondary = Color(0xFF3E2800),
    secondaryContainer = SolarGold40,
    onSecondaryContainer = Color(0xFFFFEFA7),
    tertiary = EcoCyan80,
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = EcoCyan40,
    background = DarkBackground,
    onBackground = Color(0xFFE2ECE2),
    surface = DarkSurface,
    onSurface = Color(0xFFE2ECE2),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBCCBBF)
)

private val LightColorScheme = lightColorScheme(
    primary = SolarpunkGreen40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = SolarpunkDeepForest,
    secondary = SolarSunAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = Color(0xFF4C3600),
    tertiary = EcoTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    background = LightBackground,
    onBackground = Color(0xFF142017),
    surface = LightSurface,
    onSurface = Color(0xFF142017),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF425146)
)

@Composable
fun SolarpunkFarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent tailored solarpunk aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
