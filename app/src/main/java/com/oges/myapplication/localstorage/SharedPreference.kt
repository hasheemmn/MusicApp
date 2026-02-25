package com.oges.myapplication.localstorage

import android.content.Context

class SharedPreference(context: Context) {

    private val pref = context.getSharedPreferences(
        "music_player_pref",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TITLE = "key_title"
        private const val KEY_AUTHOR = "key_author"
        private const val KEY_COVER = "key_cover"
        private const val KEY_AUDIO = "key_audio"

        private const val KEY_IS_PLAYING = "key_is_playing"
        private const val KEY_EQ_ENABLED = "key_eq_enabled"
        private const val KEY_BASS = "key_bass"
        private const val KEY_TREBLE = "key_treble"
        private const val KEY_PRESET = "key_preset"

        private const val KEY_BAND_0 = "key_band_0"
        private const val KEY_BAND_1 = "key_band_1"
        private const val KEY_BAND_2 = "key_band_2"
        private const val KEY_BAND_3 = "key_band_3"
        private const val KEY_BAND_4 = "key_band_4"
    }


    /* ---------- SET ---------- */

    fun setSong(title: String, author: String, cover: String, audioUrl: String) {
        pref.edit().apply {
            putString(KEY_TITLE, title)
            putString(KEY_AUTHOR, author)
            putString(KEY_COVER, cover)
            putString(KEY_AUDIO, audioUrl)
            apply()
        }
    }

    fun setIsMusicPlaying(isPlaying: Boolean) {
        pref.edit().putBoolean(KEY_IS_PLAYING, isPlaying).apply()
    }

    fun isMusicPlaying(): Boolean {
        return pref.getBoolean(KEY_IS_PLAYING, false)
    }

    /* ---------- GET ---------- */

    fun getTitle() = pref.getString(KEY_TITLE, "") ?: ""
    fun getAuthor() = pref.getString(KEY_AUTHOR, "") ?: ""
    fun getCover() = pref.getString(KEY_COVER, "") ?: ""
    fun getAudioUrl() = pref.getString(KEY_AUDIO, "") ?: ""

    /* ------------------ EQUALIZER SAVE ------------------ */

    fun setEqEnabled(enabled: Boolean) {
        pref.edit().putBoolean(KEY_EQ_ENABLED, enabled).apply()
    }

    fun isEqEnabled(): Boolean {
        return pref.getBoolean(KEY_EQ_ENABLED, true)
    }

    fun setBass(level: Int) {
        pref.edit().putInt(KEY_BASS, level).apply()
    }

    fun getBass(): Int {
        return pref.getInt(KEY_BASS, 0)
    }

    fun setTreble(level: Int) {
        pref.edit().putInt(KEY_TREBLE, level).apply()
    }

    fun getTreble(): Int {
        return pref.getInt(KEY_TREBLE, 0)
    }

    fun setPreset(preset: String) {
        pref.edit().putString(KEY_PRESET, preset).apply()
    }

    fun getPreset(): String {
        return pref.getString(KEY_PRESET, "Flat") ?: "Flat"
    }

    fun setBandLevel(band: Int, level: Int) {
        val key = when (band) {
            0 -> KEY_BAND_0
            1 -> KEY_BAND_1
            2 -> KEY_BAND_2
            3 -> KEY_BAND_3
            4 -> KEY_BAND_4
            else -> return
        }
        pref.edit().putInt(key, level).apply()
    }

    fun getBandLevel(band: Int): Int {
        val key = when (band) {
            0 -> KEY_BAND_0
            1 -> KEY_BAND_1
            2 -> KEY_BAND_2
            3 -> KEY_BAND_3
            4 -> KEY_BAND_4
            else -> return 0
        }
        return pref.getInt(key, 0)
    }
}