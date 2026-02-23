package com.oges.myapplication.api

import android.content.Context
import android.media.MediaPlayer
import com.oges.myapplication.models.SongModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var songs: List<SongModel> = emptyList()

    fun loadSongs(list: List<SongModel>) {
        songs = list
    }

    fun play(index: Int, onPrepared: (Int) -> Unit) {
        if (songs.isEmpty()) return

        currentIndex = index
        release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(songs[index].audioUrl)
            setOnPreparedListener {
                start()
                onPrepared(duration)
            }
            prepareAsync()
        }
    }
    fun previous(): Int {
        currentIndex =
            if (currentIndex - 1 < 0)
                songs.size - 1
            else
                currentIndex - 1

        return currentIndex
    }
    fun pause() = mediaPlayer?.pause()

    fun resume() = mediaPlayer?.start()

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun next(): Int {
        currentIndex = (currentIndex + 1) % songs.size
        return currentIndex
    }
    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}