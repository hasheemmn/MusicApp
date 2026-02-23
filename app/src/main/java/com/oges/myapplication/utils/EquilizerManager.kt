package com.oges.myapplication.utils


import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class EqualizerManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var bandCount: Short = 0
    private var minLevel: Short = -1500
    private var maxLevel: Short = 1500
    private var effectsEnabled = true

    fun init(audioSessionId: Int) {
        if (audioSessionId == 0) return

        try {
            release()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = effectsEnabled
                bandCount = numberOfBands
                val range = bandLevelRange
                minLevel = range[0]
                maxLevel = range[1]
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = effectsEnabled
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = effectsEnabled
            }

        } catch (e: Exception) {
            Log.e("EqualizerManager", "Init failed", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        effectsEnabled = enabled
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
    }

    fun setBandLevel(band: Short, level: Short) {
        if (band in 0 until bandCount) {
            equalizer?.setBandLevel(
                band,
                level.coerceIn(minLevel, maxLevel)
            )
        }
    }

    fun setBass(strength: Short) {
        if (bassBoost?.strengthSupported == true) {
            bassBoost?.setStrength(strength.coerceIn(0, 1000))
        }
    }

    fun setVirtualizer(strength: Short) {
        if (virtualizer?.strengthSupported == true) {
            virtualizer?.setStrength(strength.coerceIn(0, 1000))
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}