package com.oges.myapplication.utils

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class EqualizerManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    @Suppress("DEPRECATION")
    private var virtualizer: Virtualizer? = null

    private var bandCount: Short = 0
    private var minLevel: Short = -1500
    private var maxLevel: Short = 1500
    private var isEffectsEnabled: Boolean = true

    fun initEqualizer(audioSessionId: Int) {
        if (audioSessionId == 0) return
        
        try {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEffectsEnabled
                this@EqualizerManager.bandCount = numberOfBands
                val range = bandLevelRange
                if (range.size >= 2) {
                    this@EqualizerManager.minLevel = range[0]
                    this@EqualizerManager.maxLevel = range[1]
                }
            }
            
            bassBoost?.release()
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = isEffectsEnabled
            }
            
            virtualizer?.release()
            @Suppress("DEPRECATION")
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = isEffectsEnabled
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error initializing audio effects", e)
        }
    }

    fun setEffectsEnabled(enabled: Boolean) {
        isEffectsEnabled = enabled
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
    }

    fun isEnabled(): Boolean = isEffectsEnabled

   /* fun getBandCount(): Int = bandCount.toInt().coerceAtLeast(0)
    
    fun getBandLevelRange(): ShortArray = shortArrayOf(minLevel, maxLevel)

    fun getCenterFreq(band: Short): Int {
        if (band !in 0..<bandCount) return 0
        return equalizer?.getCenterFreq(band) ?: 0
    }*/

    fun setBandLevel(band: Short, level: Short) {
        if (band !in 0..<bandCount) return
        val clampedLevel = level.coerceIn(minLevel, maxLevel)
        equalizer?.setBandLevel(band, clampedLevel)
    }

    fun getBandLevel(band: Short): Short {
        if (band !in 0..<bandCount) return 0
        return equalizer?.getBandLevel(band) ?: 0.toShort()
    }

    fun applyPreset(presetName: String) {
        val levels = getPresetLevels(presetName)
        levels.forEachIndexed { index, level ->
            if (index < bandCount) {
                setBandLevel(index.toShort(), level.toShort())
            }
        }
    }

    private fun getPresetLevels(presetName: String): List<Int> {
        return when (presetName) {
            "Rock" -> listOf(300, 200, -100, 200, 400)
            "Pop" -> listOf(-100, 100, 300, 100, -100)
            "Jazz" -> listOf(200, 100, -200, 100, 200)
            "Classical" -> listOf(300, 200, 0, 100, 200)
            "Vocal" -> listOf(-200, -100, 300, 100, -100)
            else -> listOf(0, 0, 0, 0, 0) // Flat
        }
    }

    fun setBassLevel(level: Short) {
        if (bassBoost?.strengthSupported == true) {
            bassBoost?.setStrength(level.coerceIn(0, 1000))
        }
    }

    @Suppress("DEPRECATION")
    fun setVirtualizerLevel(level: Short) {
        if (virtualizer?.strengthSupported == true) {
            virtualizer?.setStrength(level.coerceIn(0, 1000))
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
        bandCount = 0
    }
}
