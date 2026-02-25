package com.oges.myapplication.service

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.*
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.*
import com.oges.myapplication.MainActivity
import com.oges.myapplication.R
import com.oges.myapplication.localstorage.SharedPreference
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.URL
import kotlin.concurrent.thread

class MusicService : Service() {

    private lateinit var player: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())

    private var currentIndex = 0
    private var currentTitle = ""
    private var currentAuthor = ""
    private var currentPhoto = ""
    private var currentBitmap: Bitmap? = null

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private lateinit var pref: SharedPreference

    private val CHANNEL_ID = "music_channel"
    private val NOTIFICATION_ID = 1

    companion object {

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_SEEK = "ACTION_SEEK"
        const val ACTION_REFRESH_EQ = "ACTION_REFRESH_EQ"

        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_AUTHOR = "EXTRA_AUTHOR"
        const val EXTRA_COVER_PHOTO = "EXTRA_COVER_PHOTO"
        const val EXTRA_SEEK = "EXTRA_SEEK"

        val playbackState = MutableStateFlow(
            PlaybackState(false, 0L, 0L, 0)
        )
    }

    data class PlaybackState(
        val isPlaying: Boolean,
        val position: Long,
        val duration: Long,
        val index: Int
    )

    override fun onCreate() {
        super.onCreate()

        createChannel()
        pref = SharedPreference(applicationContext)

        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    applyEqualizer()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
        })

        startForeground(NOTIFICATION_ID, buildNotification(false))
        startProgressUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {

            ACTION_PLAY -> {

                val url = intent.getStringExtra(EXTRA_URL)
                currentIndex = intent.getIntExtra(EXTRA_INDEX, 0)
                currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""
                currentAuthor = intent.getStringExtra(EXTRA_AUTHOR) ?: ""
                currentPhoto = intent.getStringExtra(EXTRA_COVER_PHOTO) ?: ""

                if (!url.isNullOrEmpty()) {
                    playNewSong(url)
                    loadCover(currentPhoto)
                } else {
                    player.playWhenReady = true
                }
            }

            ACTION_PAUSE -> {
                player.playWhenReady = false
            }

            ACTION_SEEK -> {
                val pos = intent.getLongExtra(EXTRA_SEEK, 0L)
                player.seekTo(pos)
            }

            ACTION_REFRESH_EQ -> {
                applyEqualizer()
            }
        }

        return START_STICKY
    }

    private fun playNewSong(url: String) {

        val currentUri =
            player.currentMediaItem?.localConfiguration?.uri.toString()

        if (currentUri == url) {
            player.playWhenReady = true
            return
        }

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    private fun loadCover(url: String) {
        if (url.isEmpty()) return

        thread {
            try {
                val bitmap =
                    BitmapFactory.decodeStream(URL(url).openStream())
                currentBitmap = bitmap

                Handler(Looper.getMainLooper()).post {
                    updateNotification()
                }
            } catch (_: Exception) {}
        }
    }

    /* ---------------- EQUALIZER ---------------- */

    private fun applyEqualizer() {

        try {

            val sessionId = player.audioSessionId
            if (sessionId == 0) return

            if (!pref.isEqEnabled()) {
                equalizer?.enabled = false
                bassBoost?.enabled = false
                return
            }

            // Create Equalizer only once
            if (equalizer == null) {
                equalizer = Equalizer(0, sessionId)
            }

            if (bassBoost == null) {
                bassBoost = BassBoost(0, sessionId)
            }

            equalizer?.enabled = true
            bassBoost?.enabled = true

            val bandCount = equalizer?.numberOfBands ?: return
            val range = equalizer?.bandLevelRange ?: return
            val min = range[0]
            val max = range[1]

            // Just update levels (NO recreation)
            for (i in 0 until bandCount) {

                val savedLevel = pref.getBandLevel(i)

                val safeLevel =
                    savedLevel.coerceIn(min.toInt(), max.toInt())

                equalizer?.setBandLevel(
                    i.toShort(),
                    safeLevel.toShort()
                )
            }

            // Update Bass
            val strength = pref.getBass()
                .coerceIn(0, 1000)

            bassBoost?.setStrength(strength.toShort())

            // Update Treble (last band boost)
            val lastBand = bandCount - 1
            if (lastBand >= 0) {

                val treble = pref.getTreble()
                val boost = (treble - 500) / 2

                val current =
                    equalizer?.getBandLevel(lastBand.toShort()) ?: 0

                val updated = current + boost

                val safeTreble =
                    updated.coerceIn(min.toInt(), max.toInt())

                equalizer?.setBandLevel(
                    lastBand.toShort(),
                    safeTreble.toShort()
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* ---------------- NOTIFICATION ---------------- */

    private fun updateNotification() {

        val manager =
            getSystemService(NotificationManager::class.java)

        manager.notify(
            NOTIFICATION_ID,
            buildNotification(player.isPlaying)
        )
    }

    private fun buildNotification(isPlaying: Boolean): Notification {

        val remoteViews =
            RemoteViews(packageName, R.layout.item_notification_ui)

        remoteViews.setTextViewText(R.id.tv_title, currentTitle)
        remoteViews.setTextViewText(R.id.tv_song_name, currentAuthor)

        currentBitmap?.let {
            remoteViews.setImageViewBitmap(R.id.iv_logo, it)
        }

        val playIntent =
            Intent(this, MusicService::class.java)
                .apply { action = ACTION_PLAY }

        val pauseIntent =
            Intent(this, MusicService::class.java)
                .apply { action = ACTION_PAUSE }

        remoteViews.setOnClickPendingIntent(
            R.id.iv_play_pause,
            PendingIntent.getService(
                this,
                10,
                if (isPlaying) pauseIntent else playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )
        )

        remoteViews.setImageViewResource(
            R.id.iv_play_pause,
            if (isPlaying)
                R.drawable.ic_pause_white
            else
                R.drawable.ic_white_play_icon
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_logo)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setContentIntent(createContentIntent())
            .build()
    }

    /* ---------------- PROGRESS SYNC ---------------- */

    private fun startProgressUpdates() {

        handler.post(object : Runnable {
            override fun run() {

                playbackState.value =
                    PlaybackState(
                        player.isPlaying,
                        player.currentPosition,
                        if (player.duration < 0) 0L else player.duration,
                        currentIndex
                    )

                handler.postDelayed(this, 500)
            }
        })
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun createContentIntent(): PendingIntent {

        val openIntent =
            Intent(this, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        return PendingIntent.getActivity(
            this,
            100,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        equalizer?.release()
        bassBoost?.release()
        player.release()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}