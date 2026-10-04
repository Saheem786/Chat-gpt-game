package com.example.game3d.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.data.model.WeatherType

class DayNightLightingSystem {

    data class Keyframe(
        val time: Float, // 0.0 to 24.0
        val skyColor: Color,
        val ambientColor: Color,
        val sunLightColor: Color
    )

    private val keyframes = listOf(
        // Midnight (00:00)
        Keyframe(
            time = 0.0f,
            skyColor = Color(0.035f, 0.045f, 0.12f, 1f),
            ambientColor = Color(0.12f, 0.14f, 0.22f, 1f),
            sunLightColor = Color(0.18f, 0.22f, 0.38f, 1f)
        ),
        // Deep Night (03:30)
        Keyframe(
            time = 3.5f,
            skyColor = Color(0.04f, 0.05f, 0.14f, 1f),
            ambientColor = Color(0.12f, 0.14f, 0.22f, 1f),
            sunLightColor = Color(0.20f, 0.24f, 0.40f, 1f)
        ),
        // Astronomical Dawn (05:00)
        Keyframe(
            time = 5.0f,
            skyColor = Color(0.18f, 0.12f, 0.28f, 1f),
            ambientColor = Color(0.16f, 0.16f, 0.25f, 1f),
            sunLightColor = Color(0.35f, 0.28f, 0.40f, 1f)
        ),
        // Nautical Twilight / Pink horizon (05:45)
        Keyframe(
            time = 5.75f,
            skyColor = Color(0.48f, 0.28f, 0.42f, 1f),
            ambientColor = Color(0.26f, 0.22f, 0.30f, 1f),
            sunLightColor = Color(0.70f, 0.42f, 0.35f, 1f)
        ),
        // Golden Sunrise (06:30)
        Keyframe(
            time = 6.5f,
            skyColor = Color(0.90f, 0.58f, 0.34f, 1f),
            ambientColor = Color(0.40f, 0.35f, 0.32f, 1f),
            sunLightColor = Color(0.98f, 0.74f, 0.42f, 1f)
        ),
        // Early Morning Brightening (07:30)
        Keyframe(
            time = 7.5f,
            skyColor = Color(0.62f, 0.74f, 0.90f, 1f),
            ambientColor = Color(0.44f, 0.44f, 0.46f, 1f),
            sunLightColor = Color(0.96f, 0.90f, 0.75f, 1f)
        ),
        // Clear Day (10:00)
        Keyframe(
            time = 10.0f,
            skyColor = Color(0.45f, 0.72f, 0.94f, 1f),
            ambientColor = Color(0.48f, 0.48f, 0.50f, 1f),
            sunLightColor = Color(0.98f, 0.96f, 0.90f, 1f)
        ),
        // High Noon Zenith (12:00)
        Keyframe(
            time = 12.0f,
            skyColor = Color(0.42f, 0.70f, 0.94f, 1f),
            ambientColor = Color(0.50f, 0.50f, 0.52f, 1f),
            sunLightColor = Color(1.0f, 0.98f, 0.92f, 1f)
        ),
        // Mid Afternoon (15:00)
        Keyframe(
            time = 15.0f,
            skyColor = Color(0.46f, 0.71f, 0.92f, 1f),
            ambientColor = Color(0.48f, 0.48f, 0.50f, 1f),
            sunLightColor = Color(0.98f, 0.94f, 0.84f, 1f)
        ),
        // Late Afternoon Warmth (17:00)
        Keyframe(
            time = 17.0f,
            skyColor = Color(0.65f, 0.72f, 0.88f, 1f),
            ambientColor = Color(0.45f, 0.42f, 0.42f, 1f),
            sunLightColor = Color(0.98f, 0.84f, 0.62f, 1f)
        ),
        // Golden Sunset Hour (18:00)
        Keyframe(
            time = 18.0f,
            skyColor = Color(0.94f, 0.55f, 0.30f, 1f),
            ambientColor = Color(0.42f, 0.34f, 0.30f, 1f),
            sunLightColor = Color(0.98f, 0.62f, 0.30f, 1f)
        ),
        // Fiery Dusk (19:00)
        Keyframe(
            time = 19.0f,
            skyColor = Color(0.78f, 0.34f, 0.28f, 1f),
            ambientColor = Color(0.32f, 0.25f, 0.28f, 1f),
            sunLightColor = Color(0.85f, 0.40f, 0.24f, 1f)
        ),
        // Civil Twilight (19:45)
        Keyframe(
            time = 19.75f,
            skyColor = Color(0.38f, 0.20f, 0.42f, 1f),
            ambientColor = Color(0.24f, 0.20f, 0.28f, 1f),
            sunLightColor = Color(0.55f, 0.32f, 0.42f, 1f)
        ),
        // Nightfall (21:00)
        Keyframe(
            time = 21.0f,
            skyColor = Color(0.07f, 0.08f, 0.18f, 1f),
            ambientColor = Color(0.15f, 0.16f, 0.25f, 1f),
            sunLightColor = Color(0.24f, 0.26f, 0.42f, 1f)
        ),
        // Wrapping to Midnight (24:00)
        Keyframe(
            time = 24.0f,
            skyColor = Color(0.035f, 0.045f, 0.12f, 1f),
            ambientColor = Color(0.12f, 0.14f, 0.22f, 1f),
            sunLightColor = Color(0.18f, 0.22f, 0.38f, 1f)
        )
    )

    // Output smooth colors
    val currentSkyColor = Color(0.42f, 0.70f, 0.94f, 1f)
    val currentAmbientColor = Color(0.48f, 0.48f, 0.52f, 1f)
    val currentSunColor = Color(1.0f, 0.98f, 0.92f, 1f)
    val sunDirection = Vector3(-0.4f, -0.8f, -0.4f)

    private val targetSky = Color()
    private val targetAmbient = Color()
    private val targetSun = Color()

    fun update(timeOfDay: Float, weather: WeatherType, delta: Float) {
        val normalizedTime = (timeOfDay % 24.0f + 24.0f) % 24.0f

        // Find surrounding keyframes
        var k1 = keyframes[0]
        var k2 = keyframes[1]

        for (i in 0 until keyframes.size - 1) {
            if (normalizedTime >= keyframes[i].time && normalizedTime <= keyframes[i + 1].time) {
                k1 = keyframes[i]
                k2 = keyframes[i + 1]
                break
            }
        }

        // Interpolation factor between k1 and k2
        val range = k2.time - k1.time
        val alpha = if (range > 0.0001f) MathUtils.clamp((normalizedTime - k1.time) / range, 0f, 1f) else 0f

        // Smooth cubic ease for natural sunset/sunrise transitions
        val smoothAlpha = alpha * alpha * (3f - 2f * alpha)

        // Base clear sky colors
        targetSky.set(k1.skyColor).lerp(k2.skyColor, smoothAlpha)
        targetAmbient.set(k1.ambientColor).lerp(k2.ambientColor, smoothAlpha)
        targetSun.set(k1.sunLightColor).lerp(k2.sunLightColor, smoothAlpha)

        // Apply Weather Overlays
        when (weather) {
            WeatherType.RAINY -> {
                targetSky.lerp(Color(0.32f, 0.38f, 0.46f, 1f), 0.65f)
                targetAmbient.lerp(Color(0.28f, 0.32f, 0.38f, 1f), 0.50f)
                targetSun.r *= 0.55f
                targetSun.g *= 0.55f
                targetSun.b *= 0.60f
            }
            WeatherType.PARTLY_CLOUDY -> {
                targetSky.lerp(Color(0.50f, 0.68f, 0.85f, 1f), 0.30f)
                targetAmbient.lerp(Color(0.42f, 0.44f, 0.48f, 1f), 0.20f)
                targetSun.r *= 0.88f
                targetSun.g *= 0.88f
                targetSun.b *= 0.90f
            }
            WeatherType.WIND_STORM -> {
                targetSky.lerp(Color(0.22f, 0.26f, 0.32f, 1f), 0.70f)
                targetAmbient.lerp(Color(0.24f, 0.24f, 0.28f, 1f), 0.55f)
                targetSun.r *= 0.45f
                targetSun.g *= 0.45f
                targetSun.b *= 0.50f
            }
            WeatherType.HEATWAVE -> {
                targetSky.lerp(Color(0.96f, 0.88f, 0.72f, 1f), 0.35f)
                targetSun.r = (targetSun.r * 1.15f).coerceAtMost(1.0f)
                targetSun.g = (targetSun.g * 1.05f).coerceAtMost(1.0f)
                targetSun.b = (targetSun.b * 0.90f)
            }
            WeatherType.DROUGHT -> {
                targetSky.lerp(Color(0.85f, 0.80f, 0.70f, 1f), 0.25f)
                targetSun.r = (targetSun.r * 1.10f).coerceAtMost(1.0f)
                targetSun.g = (targetSun.g * 1.02f).coerceAtMost(1.0f)
            }
            WeatherType.SUNNY -> {
                // Clear natural sky
            }
        }

        // Smoothly blend to targets to eliminate any snapping
        val blendRate = MathUtils.clamp(delta * 5.0f, 0f, 1f)
        currentSkyColor.lerp(targetSky, blendRate)
        currentAmbientColor.lerp(targetAmbient, blendRate)
        currentSunColor.lerp(targetSun, blendRate)

        // Calculate dynamic Sun/Moon celestial orbital trajectory
        // Sun rises in East (6:00) -> Zenith (12:00) -> Sets in West (18:00)
        // Moon rises in East (18:00) -> High Midnight (00:00) -> Sets in West (06:00)
        val sunRad = (normalizedTime - 6.0f) / 12.0f * MathUtils.PI
        val isDay = normalizedTime in 5.5f..18.5f

        val sunX = -MathUtils.cos(sunRad)
        val sunY = -MathUtils.sin(sunRad).coerceAtLeast(0.15f) // Sun shines downward
        val sunZ = -0.35f

        if (isDay) {
            sunDirection.set(sunX, -sunY, sunZ).nor()
        } else {
            // Moon direction
            val moonRad = (normalizedTime - 18.0f) / 12.0f * MathUtils.PI
            val moonX = -MathUtils.cos(moonRad)
            val moonY = -MathUtils.sin(moonRad).coerceAtLeast(0.20f)
            sunDirection.set(moonX, -moonY, 0.35f).nor()
        }
    }
}
