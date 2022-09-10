package com.devhomc.videoplayersample.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import com.devhomc.videoplayersample.ui.player.PlayerActivity
import com.google.common.util.concurrent.Futures

class PlayerService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private val mediaSessionCallback = object : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controllerInfo: ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ) = Futures.immediateFuture(mediaItems.map { mediaItem ->
            mediaItem.buildUpon()
                .setUri(mediaItem.requestMetadata.mediaUri)
                .build()
        })
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        val intent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(mediaSessionCallback)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: ControllerInfo) = mediaSession
}
