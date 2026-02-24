package com.oges.myapplication.vm

import android.app.Application
import android.content.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.oges.myapplication.models.*
import com.oges.myapplication.service.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class HomeViewModel(application: Application) :
    AndroidViewModel(application) {

    private val app = getApplication<Application>()
    private lateinit var receiver: BroadcastReceiver

    private val _songs = MutableStateFlow<List<SongModel>>(emptyList())
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
        registerReceiver()
        startServiceEarly()
    }

    private fun startServiceEarly() {
        val intent = Intent(app, MusicService::class.java)
        ContextCompat.startForegroundService(app, intent)
    }

    private fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = app.assets.open("music.json")
                .bufferedReader().use { it.readText() }

            val response =
                Gson().fromJson(json, SongResponse::class.java)

            _songs.value = response.songs
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

        // 🔥 If nothing prepared yet → play current song
        if (_duration.value == 0L) {
            playSong(_currentIndex.value)
            return
        }

        val intent = Intent(app, MusicService::class.java).apply {
            action =
                if (_isPlaying.value)
                    MusicService.ACTION_PAUSE
                else
                    MusicService.ACTION_PLAY
        }

        app.startService(intent)
    }

    fun onNext() {
        val list = _songs.value
        if (list.isEmpty()) return

        val next = (_currentIndex.value + 1) % list.size
        playSong(next)
    }

    fun onPrevious() {
        val list = _songs.value
        if (list.isEmpty()) return

        val prev =
            if (_currentIndex.value - 1 < 0)
                list.size - 1
            else
                _currentIndex.value - 1

        playSong(prev)
    }

    fun seekTo(pos: Long) {
        val intent = Intent(app, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK
            putExtra(MusicService.EXTRA_SEEK, pos)
        }
        app.startService(intent)
    }

    private fun registerReceiver() {

        receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {

                when (intent?.action) {

                    MusicService.ACTION_NEXT -> onNext()

                    MusicService.ACTION_PREV -> onPrevious()

                    MusicService.ACTION_UPDATE -> {

                        _isPlaying.value =
                            intent.getBooleanExtra(
                                MusicService.EXTRA_IS_PLAYING,
                                false
                            )

                        _position.value =
                            intent.getLongExtra(
                                MusicService.EXTRA_POSITION,
                                0L
                            )

                        _duration.value =
                            intent.getLongExtra(
                                MusicService.EXTRA_DURATION,
                                0L
                            )

                        _currentIndex.value =
                            intent.getIntExtra(
                                MusicService.EXTRA_INDEX_UPDATE,
                                0
                            )
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(MusicService.ACTION_UPDATE)
            addAction(MusicService.ACTION_NEXT)
            addAction(MusicService.ACTION_PREV)
        }

        app.registerReceiver(receiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        app.unregisterReceiver(receiver)
    }
}