package com.oges.myapplication.api

import android.content.Context
import com.oges.myapplication.localstorage.SharedPreference
import com.oges.myapplication.models.SongModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var currentIndex = 0
    private var songs: List<SongModel> = emptyList()

    private val sharedPref = SharedPreference(context)

    fun loadSongs(list: List<SongModel>) {
        songs = list
    }

    fun getSongs(): List<SongModel> = songs

    fun getCurrentIndex(): Int = currentIndex

    fun getCurrentSong(): SongModel? {
        return songs.getOrNull(currentIndex)
    }

    fun setCurrentIndex(index: Int) {
        currentIndex = index
    }

    fun next(): SongModel? {
        if (songs.isEmpty()) return null
        currentIndex = (currentIndex + 1) % songs.size
        return getCurrentSong()
    }

    fun previous(): SongModel? {
        if (songs.isEmpty()) return null
        currentIndex =
            if (currentIndex - 1 < 0)
                songs.size - 1
            else
                currentIndex - 1

        return getCurrentSong()
    }

    fun saveSong(song: SongModel) {
        sharedPref.setSong(
            song.title,
            song.author,
            song.coverImage,
            song.audioUrl
        )
    }

    fun getSavedAudioUrl(): String {
        return sharedPref.getAudioUrl()
    }
}