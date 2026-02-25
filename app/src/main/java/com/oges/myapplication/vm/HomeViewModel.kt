package com.oges.myapplication.vm

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.oges.myapplication.models.*
import com.oges.myapplication.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val app: Context
) : ViewModel() {

    private val _songs =
        MutableStateFlow<List<SongModel>>(emptyList())
    val songs: StateFlow<List<SongModel>> = _songs

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    init {
        loadSongs()
        observePlayback()
        startService()
    }

    private fun startService() {
        val intent = Intent(app, MusicService::class.java)
        ContextCompat.startForegroundService(app, intent)
    }

    private fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {

            val json =
                app.assets.open("music.json")
                    .bufferedReader().use { it.readText() }

            val response =
                Gson().fromJson(json, SongResponse::class.java)

            _songs.value = response.songs

            // 🔥 PLAY FIRST SONG AFTER LOADING
            if (response.songs.isNotEmpty()) {
                launch(Dispatchers.Main) {
                    playSong(0)
                }
            }
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            MusicService.playbackState.collect { state ->
                _isPlaying.value = state.isPlaying
                _position.value = state.position
                _duration.value = state.duration
                _currentIndex.value = state.index
            }
        }
    }

    fun playSong(index: Int) {

        val list = _songs.value
        if (list.isEmpty()) return

        val song = list[index]
        _currentIndex.value = index

        val intent = Intent(app, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY
            putExtra(MusicService.EXTRA_URL, song.audioUrl)
            putExtra(MusicService.EXTRA_INDEX, index)
            putExtra(MusicService.EXTRA_TITLE, song.title)
            putExtra(MusicService.EXTRA_AUTHOR, song.author)
            putExtra(MusicService.EXTRA_COVER_PHOTO, song.coverImage)
        }

        app.startService(intent)
    }

    fun onPlayPause() {
        val intent = Intent(app, MusicService::class.java).apply {
            action =
                if (_isPlaying.value)
                    MusicService.ACTION_PAUSE
                else
                    MusicService.ACTION_PLAY
        }
        app.startService(intent)
    }

    fun seekTo(pos: Long) {
        val intent = Intent(app, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK
            putExtra(MusicService.EXTRA_SEEK, pos)
        }
        app.startService(intent)
    }
    fun onNext() {
        val list = _songs.value

        if (list.isEmpty()) {
            println("Songs not loaded yet")
            return
        }

        val nextIndex =
            (_currentIndex.value + 1) % list.size

        playSong(nextIndex)
    }
    fun onPrevious() {
        val list = _songs.value
        if (list.isEmpty()) return

        val prevIndex =
            if (_currentIndex.value - 1 < 0)
                list.size - 1
            else
                _currentIndex.value - 1

        playSong(prevIndex)
    }
}