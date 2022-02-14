package com.audioburst.player.core.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.PlaybackState
import com.audioburst.library.utils.PlaybackStateListener
import com.audioburst.player.core.AudioburstPlayerCore
import com.audioburst.player.core.di.Injector
import com.audioburst.player.core.media.events.PlayerEvent
import com.audioburst.player.core.media.events.PlayerEventFlow
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class MediaService : MediaBrowserServiceCompat() {
    lateinit var scope: CoroutineScope
    lateinit var exoPlayer: ExoPlayer
    lateinit var burstPlayer: BurstExoPlayer
    lateinit var audioburstLibrary: AudioburstLibrary
    lateinit var mediaControllerCallback: MediaControllerCallback
    lateinit var notificationBuilderFactory: NotificationBuilder.Factory
    lateinit var playerEventFlow: PlayerEventFlow
    lateinit var playbackNotificationController: PlaybackNotificationController

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var notificationBuilder: NotificationBuilder

    private val tag = this::class.java.simpleName
    private var isForegroundService = false

    override fun onCreate() {
        if (!AudioburstPlayerCore.isInjected) {
            AudioburstPlayerCore.init(this)
        }
        Injector.inject(this)
        super.onCreate()

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val activityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(this, ACTIVITY_REQUEST_CODE, sessionIntent, flag)
        }

        mediaSession = MediaSessionCompat(this, tag).apply {
            setSessionActivity(activityPendingIntent)
            isActive = true
        }

        mediaController = MediaControllerCompat(this, mediaSession).apply {
            registerCallback(mediaControllerCallback)
        }

        sessionToken = mediaSession.sessionToken
        notificationBuilder = notificationBuilderFactory.create(
            sessionToken = mediaSession.sessionToken,
            notificationListener = PlayerNotificationListener(),
        )

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setQueueNavigator(MediaQueueNavigator(mediaSession))
        }

        if (playbackNotificationController.shouldDisplayNotification.value) {
            notificationBuilder.showNotificationForPlayer(exoPlayer)
        }

        observePlayingState()
        observeNotificationFlag()
        audioburstLibrary.setPlaybackStateListener(playbackStateListener)
    }

    private fun observePlayingState() {
        playerEventFlow()
            .onEach { playerEvent ->
                when (playerEvent) {
                    is PlayerEvent.Error.UnsupportedUrlException -> { }
                    is PlayerEvent.IsPlayingChanged -> onIsPlayingChanged(playerEvent)
                    is PlayerEvent.PlayerStateChanged -> onPlayerStateChanged(playerEvent)
                }
            }
            .launchIn(scope)
    }

    private fun observeNotificationFlag() {
        playbackNotificationController.shouldDisplayNotification
            .onEach { shouldDisplayPlaybackNotification ->
                if (!shouldDisplayPlaybackNotification) {
                    removeNowPlayingNotification()
                } else {
                    notificationBuilder.showNotificationForPlayer(exoPlayer)
                    notificationBuilder.invalidate()
                }
            }
            .launchIn(scope)
    }

    private fun onIsPlayingChanged(isPlayingChanged: PlayerEvent.IsPlayingChanged) {
        if (isPlayingChanged.isPlaying) {
            audioburstLibrary.start()
        } else {
            audioburstLibrary.stop()
        }
    }

    private val playbackStateListener: PlaybackStateListener = PlaybackStateListener {
        burstPlayer.currentMediaUrl?.let { url ->
            PlaybackState(
                positionMillis = burstPlayer.currentPlaybackPosition.milliseconds.toLong(),
                url = url,
            )
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        stop()
    }

    override fun onDestroy() {
        stop()
    }

    private fun stop() {
        mediaSession.release()
        burstPlayer.clear()
        scope.cancel()
        audioburstLibrary.removePlaybackStateListener(playbackStateListener)
        removeNowPlayingNotification()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

    override fun onSearch(searchQuery: String, extras: Bundle?, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

    private fun removeNowPlayingNotification() {
        notificationBuilder.hideNotification()
    }

    private fun onPlayerStateChanged(playbackStateChanged: PlayerEvent.PlayerStateChanged) {
        val (playWhenReady, playbackState) = playbackStateChanged
        when (playbackState) {
            Player.STATE_BUFFERING, Player.STATE_READY -> {
                if (playbackNotificationController.shouldDisplayNotification.value) {
                    notificationBuilder.showNotificationForPlayer(exoPlayer)
                    if (playbackState == Player.STATE_READY) {
                        if (!playWhenReady) {
                            stopForeground(false)
                        }
                    }
                } else {
                    removeNowPlayingNotification()
                }
            }
            else -> removeNowPlayingNotification()
        }
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MediaService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    companion object {
        private const val ROOT_ID = "root"
        private const val ACTIVITY_REQUEST_CODE = 0x213
    }
}
