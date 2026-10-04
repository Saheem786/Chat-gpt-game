package com.example.game3d.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.example.data.model.AnimalSpecies
import com.example.game3d.world.Animal3DEntity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SpatialLivestockAudioSystem {

    private val sampleRate = 44100
    private val audioExecutor: ExecutorService = Executors.newFixedThreadPool(2)

    // Pre-synthesized PCM waveform buffers for each species
    private val soundBuffers = ConcurrentHashMap<AnimalSpecies, ShortArray>()

    // Per-animal ambient cooldown timers (animalId -> remainingSeconds)
    private val animalSoundTimers = ConcurrentHashMap<Long, Float>()

    // Continuous Bee Apiary ambient drone tracker
    private var beeDroneTimer = 0f

    private var isMuted = false

    init {
        generateSpeciesAudioBuffers()
    }

    private fun generateSpeciesAudioBuffers() {
        // 1. Chicken: Double-cluck staccato chirp (650Hz -> 900Hz -> 500Hz)
        soundBuffers[AnimalSpecies.CHICKEN] = synthesizeCluckSound()

        // 2. Duck: Nasal harmonic quack (440Hz -> 300Hz with formants)
        soundBuffers[AnimalSpecies.DUCK] = synthesizeQuackSound()

        // 3. Cow: Resonant warm lowing moo (125Hz with 2nd and 3rd harmonics)
        soundBuffers[AnimalSpecies.COW] = synthesizeMooSound()

        // 4. Goat: Agitated bleat (280Hz with 12Hz vibrato pitch wobble)
        soundBuffers[AnimalSpecies.GOAT] = synthesizeGoatBleatSound()

        // 5. Sheep: Soft warm baa (210Hz with 7Hz tremolo)
        soundBuffers[AnimalSpecies.SHEEP] = synthesizeSheepBaaSound()

        // 6. Pig: Guttural short grunt/oink (110Hz -> 160Hz with low frequency grit)
        soundBuffers[AnimalSpecies.PIG] = synthesizePigGruntSound()

        // 7. Bees: Warm harmonic apiary buzz (230Hz fundamental + 460Hz overtone)
        soundBuffers[AnimalSpecies.BEES] = synthesizeBeeBuzzSound()
    }

    fun update(
        delta: Float,
        playerPos: Vector3,
        playerYaw: Float,
        animals: List<Animal3DEntity>
    ) {
        if (isMuted) return

        val maxHearingDistance = 8.5f

        for (animal in animals) {
            val dist = playerPos.dst(animal.position)

            // Animals only trigger ambient sounds if within hearing distance
            if (dist <= maxHearingDistance) {
                val currentTimer = animalSoundTimers[animal.entityId] ?: (Random.nextFloat() * 4f + 2f)
                val newTimer = currentTimer - delta

                if (newTimer <= 0f) {
                    // Reset timer with randomized interval (4 to 9 seconds)
                    animalSoundTimers[animal.entityId] = Random.nextFloat() * 5f + 4f
                    playSpatializedSound(animal.species, animal.position, playerPos, playerYaw, dist, maxHearingDistance)
                } else {
                    animalSoundTimers[animal.entityId] = newTimer
                }
            } else {
                // If player is far away, tick down slowly so they don't immediately burst when approaching
                val currentTimer = animalSoundTimers[animal.entityId] ?: 3f
                animalSoundTimers[animal.entityId] = Math.max(currentTimer - delta, 1.5f)
            }
        }

        // Ambient Bee drone update
        val beeHive = animals.find { it.species == AnimalSpecies.BEES }
        if (beeHive != null) {
            val distToBees = playerPos.dst(beeHive.position)
            if (distToBees <= maxHearingDistance) {
                beeDroneTimer -= delta
                if (beeDroneTimer <= 0f) {
                    beeDroneTimer = 2.2f // Loop duration
                    playSpatializedSound(AnimalSpecies.BEES, beeHive.position, playerPos, playerYaw, distToBees, maxHearingDistance, volumeScale = 0.55f)
                }
            }
        }
    }

    fun playInteractionSound(
        species: AnimalSpecies,
        animalPos: Vector3,
        playerPos: Vector3,
        playerYaw: Float
    ) {
        val dist = playerPos.dst(animalPos)
        playSpatializedSound(species, animalPos, playerPos, playerYaw, dist, 12f, volumeScale = 1.0f)
    }

    private fun playSpatializedSound(
        species: AnimalSpecies,
        sourcePos: Vector3,
        listenerPos: Vector3,
        listenerYaw: Float,
        distance: Float,
        maxDistance: Float,
        volumeScale: Float = 0.85f
    ) {
        val pcm = soundBuffers[species] ?: return

        // 1. Distance Attenuation (Inverse-distance falloff with minimum distance)
        val minDistance = 1.5f
        val clampedDist = MathUtils.clamp(distance, minDistance, maxDistance)
        val linearFactor = 1.0f - ((clampedDist - minDistance) / (maxDistance - minDistance))
        val distanceVolume = (linearFactor * linearFactor) * volumeScale

        if (distanceVolume <= 0.01f) return

        // 2. Stereo Panning (Calculated relative to player camera/facing orientation)
        val dx = sourcePos.x - listenerPos.x
        val dz = sourcePos.z - listenerPos.z

        // Convert world delta vector to listener's local coordinate frame
        val yawRad = listenerYaw * MathUtils.degreesToRadians
        val cosYaw = MathUtils.cos(yawRad)
        val sinYaw = MathUtils.sin(yawRad)

        // Local X is right (+), local Z is forward (+)
        val localX = dx * cosYaw - dz * sinYaw
        val pan = MathUtils.clamp(if (distance > 0.1f) (localX / distance) else 0f, -1.0f, 1.0f)

        // Calculate left & right stereo volume gains
        val leftGain = distanceVolume * Math.sqrt((1.0 - pan) / 2.0).toFloat()
        val rightGain = distanceVolume * Math.sqrt((1.0 + pan) / 2.0).toFloat()

        // Render stereo audio stream asynchronously
        audioExecutor.execute {
            playStereoPcm(pcm, leftGain, rightGain)
        }
    }

    private fun playStereoPcm(monoPcm: ShortArray, leftGain: Float, rightGain: Float) {
        val stereoFrames = monoPcm.size
        val stereoBuffer = ShortArray(stereoFrames * 2)

        for (i in 0 until stereoFrames) {
            val sample = monoPcm[i].toInt()
            val leftSample = (sample * leftGain).toInt().coerceIn(-32768, 32767)
            val rightSample = (sample * rightGain).toInt().coerceIn(-32768, 32767)
            stereoBuffer[i * 2] = leftSample.toShort()
            stereoBuffer[i * 2 + 1] = rightSample.toShort()
        }

        try {
            val bufferSizeBytes = stereoBuffer.size * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(stereoBuffer, 0, stereoBuffer.size)
            audioTrack.play()

            // Release AudioTrack after playback finishes
            val durationMs = (stereoFrames * 1000L) / sampleRate + 100L
            Thread.sleep(durationMs)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignored if audio track is interrupted
        }
    }

    // --- PROCEDURAL ANIMAL SOUND SYNTHESIZERS ---

    private fun synthesizeCluckSound(): ShortArray {
        val durationMs = 320
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Two rapid chirps: pulse 1 (0 to 120ms), pulse 2 (160ms to 280ms)
            var amp = 0.0
            var freq = 650.0

            if (t < 0.12) {
                val pt = t / 0.12
                amp = sin(pt * PI)
                freq = 650.0 + 300.0 * sin(pt * PI)
            } else if (t in 0.16..0.28) {
                val pt = (t - 0.16) / 0.12
                amp = sin(pt * PI) * 0.85
                freq = 700.0 + 250.0 * sin(pt * PI)
            }

            val wave = sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)
            val sample = (wave * amp * 20000.0).toInt().coerceIn(-32768, 32767)
            pcm[i] = sample.toShort()
        }
        return pcm
    }

    private fun synthesizeQuackSound(): ShortArray {
        val durationMs = 450
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = if (t < 0.08) (t / 0.08) else (1.0 - (t - 0.08) / 0.37).coerceAtLeast(0.0)
            val freq = 420.0 - 120.0 * (t / 0.45)

            // Nasal formants
            val fundamental = sin(2.0 * PI * freq * t)
            val harmonic2 = 0.6 * sin(4.0 * PI * freq * t)
            val harmonic3 = 0.35 * sin(6.0 * PI * freq * t)

            val wave = (fundamental + harmonic2 + harmonic3) * env
            pcm[i] = (wave * 19000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    private fun synthesizeMooSound(): ShortArray {
        val durationMs = 1200
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = when {
                t < 0.25 -> (t / 0.25)
                t > 0.85 -> (1.0 - (t - 0.85) / 0.35).coerceAtLeast(0.0)
                else -> 1.0
            }
            val freq = 125.0 + 15.0 * sin(t * PI * 1.5)

            val fundamental = sin(2.0 * PI * freq * t)
            val harmonic2 = 0.5 * sin(4.0 * PI * freq * t)
            val harmonic3 = 0.25 * sin(6.0 * PI * freq * t)
            val harmonic4 = 0.12 * sin(8.0 * PI * freq * t)

            val wave = (fundamental + harmonic2 + harmonic3 + harmonic4) * env
            pcm[i] = (wave * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    private fun synthesizeGoatBleatSound(): ShortArray {
        val durationMs = 750
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = if (t < 0.1) (t / 0.1) else (1.0 - (t - 0.1) / 0.65).coerceAtLeast(0.0)
            // 12Hz pitch modulation (vibrato)
            val vibrato = 22.0 * sin(2.0 * PI * 12.0 * t)
            val freq = 280.0 + vibrato

            val fundamental = sin(2.0 * PI * freq * t)
            val harmonic = 0.45 * sin(4.0 * PI * freq * t)

            val wave = (fundamental + harmonic) * env
            pcm[i] = (wave * 19000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    private fun synthesizeSheepBaaSound(): ShortArray {
        val durationMs = 850
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = if (t < 0.15) (t / 0.15) else (1.0 - (t - 0.15) / 0.70).coerceAtLeast(0.0)
            // 8Hz gentle vibrato
            val vibrato = 14.0 * sin(2.0 * PI * 8.0 * t)
            val freq = 210.0 + vibrato

            val fundamental = sin(2.0 * PI * freq * t)
            val harmonic2 = 0.4 * sin(4.0 * PI * freq * t)
            val harmonic3 = 0.2 * sin(6.0 * PI * freq * t)

            val wave = (fundamental + harmonic2 + harmonic3) * env
            pcm[i] = (wave * 20000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    private fun synthesizePigGruntSound(): ShortArray {
        val durationMs = 380
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = if (t < 0.06) (t / 0.06) else (1.0 - (t - 0.06) / 0.32).coerceAtLeast(0.0)
            val freq = 105.0 + 35.0 * sin(t * PI * 3.0)

            val fundamental = sin(2.0 * PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.25
            val harmonic = 0.4 * sin(3.0 * PI * freq * t)

            val wave = (fundamental + harmonic + noise) * env
            pcm[i] = (wave * 18000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    private fun synthesizeBeeBuzzSound(): ShortArray {
        val durationMs = 2200
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = sin(t / 2.2 * PI) // Smooth loop envelope

            val f1 = 230.0 + 8.0 * sin(t * 14.0)
            val f2 = 460.0 + 12.0 * sin(t * 11.0)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.15

            val wave = (0.6 * sin(2.0 * PI * f1 * t) + 0.4 * sin(2.0 * PI * f2 * t) + noise) * env
            pcm[i] = (wave * 14000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    fun dispose() {
        audioExecutor.shutdownNow()
        soundBuffers.clear()
        animalSoundTimers.clear()
    }
}
