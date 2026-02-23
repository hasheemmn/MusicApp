package com.oges.myapplication.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.oges.myapplication.api.AudioRepository
import com.oges.myapplication.models.SongModel
import com.oges.myapplication.models.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: AudioRepository
) : AndroidViewModel(application) {

    private val _songs = MutableStateFlow<List<SongModel>>(emptyList())
    val songs: StateFlow<List<SongModel>> = _songs

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    init {
        loadSongs()
    }

    private fun loadSongs()
    {
        viewModelScope.launch(Dispatchers.IO) {

            val json = getApplication<Application>()
                .assets.open("music.json")
                .bufferedReader()
                .use { it.readText() }

            val response =
                Gson().fromJson(json, SongResponse::class.java)

            _songs.value = response.songs
            repository.loadSongs(response.songs)

            if (response.songs.isNotEmpty()) {
                onStart()
            }
        }
    }
    fun onPrevious() {
        val newIndex = repository.previous()
        _currentIndex.value = newIndex
        onStart()
    }
    fun onStart() {
        repository.play(_currentIndex.value) { duration ->
            _duration.value = duration
            startProgressTracking()
        }
        _isPlaying.value = true
    }

    fun onPlayPause() {
        if (repository.isPlaying()) {
            repository.pause()
            _isPlaying.value = false
        } else {
            repository.resume()
            _isPlaying.value = true
            startProgressTracking()
        }
    }

    fun onNext() {
        val newIndex = repository.next()
        _currentIndex.value = newIndex
        onStart()
    }

    private fun startProgressTracking() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                _currentPosition.value =
                    repository.getCurrentPosition()
                delay(500)
            }
        }
    }

    fun seekTo(position: Int) {
        repository.seekTo(position)
    }

    override fun onCleared() {
        repository.release()
        super.onCleared()
    }

}