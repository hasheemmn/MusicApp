package com.oges.myapplication.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.*
import com.oges.myapplication.MainActivity
import com.oges.myapplication.R
import java.net.URL
import kotlin.concurrent.thread

class MusicService : Service() {

    private lateinit var player: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())

    private var currentIndex = 0
    private var currentTitle = ""
    private var currentAuthor = ""
    private var currentPhoto = ""

    private val CHANNEL_ID = "music_channel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_SEEK = "ACTION_SEEK"

        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_AUTHOR = "EXTRA_AUTHOR"
        const val EXTRA_COVER_PHOTO = "EXTRA_COVER_PHOTO"
        const val EXTRA_SEEK = "EXTRA_SEEK"

        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING"
        const val EXTRA_POSITION = "EXTRA_POSITION"
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_INDEX_UPDATE = "EXTRA_INDEX_UPDATE"
    }

    override fun onCreate() {
        super.onCreate()

        createChannel()

        startForeground(NOTIFICATION_ID, buildNotification(false))

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1500, 3000, 1000, 1500)
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()

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
                    play(url)
                } else {
                    player.play()
                }
            }

            ACTION_PAUSE -> player.pause()

            ACTION_NEXT -> sendBroadcast(Intent(ACTION_NEXT))

            ACTION_PREV -> sendBroadcast(Intent(ACTION_PREV))

            ACTION_SEEK -> {
                val pos = intent.getLongExtra(EXTRA_SEEK, 0L)
                player.seekTo(pos)
            }
        }

        updateNotification()
        return START_STICKY
    }

    private fun play(url: String) {

        val currentUri =
            player.currentMediaItem?.localConfiguration?.uri.toString()

        if (currentUri == url) {
            player.play()
            return
        }

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    /* ---------------- NOTIFICATION ---------------- */

    private fun updateNotification() {
        startForeground(
            NOTIFICATION_ID,
            buildNotification(player.isPlaying)
        )
    }

    private fun buildNotification(isPlaying: Boolean): Notification {

        val remoteViews =
            RemoteViews(packageName, R.layout.item_notification_ui)

        remoteViews.setTextViewText(R.id.tv_title, currentTitle)
        remoteViews.setTextViewText(R.id.tv_song_name, currentAuthor)

        if (currentPhoto.isNotEmpty()) {
            thread {
                try {
                    val url = URL(currentPhoto)
                    val bitmap =
                        BitmapFactory.decodeStream(url.openStream())

                    remoteViews.setImageViewBitmap(
                        R.id.iv_logo,
                        bitmap
                    )

                    val notification =
                        NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_music_logo)
                            .setCustomContentView(remoteViews)
                            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                            .setOnlyAlertOnce(true)
                            .setOngoing(isPlaying)
                            .setContentIntent(createContentIntent())
                            .build()

                    startForeground(NOTIFICATION_ID, notification)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val playIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PLAY
        }

        val pauseIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PAUSE
        }

        val nextIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_NEXT
        }

        val prevIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PREV
        }

        remoteViews.setOnClickPendingIntent(
            R.id.iv_play_pause,
            PendingIntent.getService(
                this, 0,
                if (isPlaying) pauseIntent else playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        remoteViews.setOnClickPendingIntent(
            R.id.iv_next_not,
            PendingIntent.getService(
                this, 1,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        remoteViews.setOnClickPendingIntent(
            R.id.iv_prev,
            PendingIntent.getService(
                this, 2,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
            .setContentIntent(createContentIntent())
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .build()
    }

    /* ---------------- SYNC SEEKBAR ---------------- */

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {

                val duration =
                    if (player.duration < 0) 0L else player.duration

                val intent = Intent(ACTION_UPDATE).apply {
                    putExtra(EXTRA_IS_PLAYING, player.isPlaying)
                    putExtra(EXTRA_POSITION, player.currentPosition)
                    putExtra(EXTRA_DURATION, duration)
                    putExtra(EXTRA_INDEX_UPDATE, currentIndex)
                }

                sendBroadcast(intent)
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

    override fun onDestroy() {
        player.release()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
    private fun createContentIntent(): PendingIntent {

        val openIntent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        return PendingIntent.getActivity(
            this,
            100,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onBind(intent: Intent?) = null
}