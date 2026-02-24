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
}