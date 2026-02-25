package com.oges.myapplication.vm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.oges.myapplication.localstorage.SharedPreference
import com.oges.myapplication.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class EqualizerVM @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val pref = SharedPreference(context)

    private val TAG = "EqualizerVM"

    private val bandLevels = ShortArray(5) { 0 }

    private var bassLevel = 500
    private var trebleLevel = 500
    private var currentPreset = "Flat"

    init {
        restoreFromPref()
    }

    /* ---------------- GETTERS ---------------- */

    fun getBandLevel(band: Short): Short = bandLevels[band.toInt()]
    fun getBassLevel(): Int = bassLevel
    fun getTrebleLevel(): Int = trebleLevel
    fun getCurrentPreset(): String = currentPreset

    /* ---------------- SETTERS ---------------- */

    fun setBandLevel(band: Short, level: Short) {
        bandLevels[band.toInt()] = level
    }

    fun setBassLevel(level: Int) {
        bassLevel = level
    }

    fun setTrebleLevel(level: Int) {
        trebleLevel = level
    }

    /* ---------------- PRESETS ---------------- */

    fun setPreset(preset: String) {

        currentPreset = preset

        when (preset) {

            "Flat" -> {
                setBands(0, 0, 0, 0, 0)
                bassLevel = 500
                trebleLevel = 500
            }

            "Rock" -> {
                setBands(400, 200, -100, 300, 500)
                bassLevel = 750
                trebleLevel = 700
            }

            "Pop" -> {
                setBands(200, 300, 100, 300, 200)
                bassLevel = 650
                trebleLevel = 650
            }

            "Jazz" -> {
                setBands(100, 250, 200, 250, 150)
                bassLevel = 600
                trebleLevel = 600
            }

            "Classical" -> {
                setBands(0, 100, 200, 300, 400)
                bassLevel = 450
                trebleLevel = 750
            }

            "Vocal" -> {
                setBands(-200, 200, 400, 300, 100)
                bassLevel = 400
                trebleLevel = 650
            }
        }
    }

    private fun setBands(b0: Int, b1: Int, b2: Int, b3: Int, b4: Int) {
        bandLevels[0] = b0.toShort()
        bandLevels[1] = b1.toShort()
        bandLevels[2] = b2.toShort()
        bandLevels[3] = b3.toShort()
        bandLevels[4] = b4.toShort()
    }

    /* ---------------- SAVE + APPLY ---------------- */

    fun applyChanges() {

        Log.d(TAG, "Saving Equalizer Settings")

        pref.setPreset(currentPreset)
        pref.setBass(bassLevel)
        pref.setTreble(trebleLevel)

        for (i in 0 until 5) {
            pref.setBandLevel(i, bandLevels[i].toInt())
        }

        // 🔥 Notify Service to refresh EQ
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_REFRESH_EQ
        }
        context.startService(intent)
    }

    /* ---------------- RESTORE ---------------- */

    private fun restoreFromPref() {

        currentPreset = pref.getPreset()
        bassLevel = pref.getBass()
        trebleLevel = pref.getTreble()

        for (i in 0 until 5) {
            bandLevels[i] = pref.getBandLevel(i).toShort()
        }
    }
}