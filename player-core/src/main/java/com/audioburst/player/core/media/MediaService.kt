package com.audioburst.player.core.media

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.PlaybackState
import com.audioburst.library.utils.PlaybackStateListener
import com.audioburst.player.core.di.Injector
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

internal class MediaService : MediaBrowserServiceCompat() {
    lateinit var scope: CoroutineScope
    lateinit var exoPlayer: ExoPlayer
    lateinit var burstPlayer: BurstExoPlayer
    lateinit var audioburstLibrary: AudioburstLibrary
    lateinit var mediaControllerCallback: MediaControllerCallback

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val tag = this::class.java.simpleName

    override fun onCreate() {
        Injector.inject(this)
        super.onCreate()

        val activityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(this, ACTIVITY_REQUEST_CODE, sessionIntent, ACTIVITY_NO_FLAGS)
        }

        mediaSession = MediaSessionCompat(this, tag).apply {
            setSessionActivity(activityPendingIntent)
            isActive = true
        }

        mediaController = MediaControllerCompat(this, mediaSession).apply {
            registerCallback(mediaControllerCallback)
        }

        sessionToken = mediaSession.sessionToken

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setQueueNavigator(MediaQueueNavigator(mediaSession))
        }

        observePlayingState()
        audioburstLibrary.setPlaybackStateListener(playbackStateListener)
    }

    private fun observePlayingState() {
        burstPlayer.playbackState
            .map { it.isPlaying }
            .debounce(timeoutMillis = 100)
            .distinctUntilChanged()
            .onEach { isPlaying ->
                if (isPlaying) {
                    audioburstLibrary.start()
                } else {
                    audioburstLibrary.stop()
                }
            }
            .launchIn(scope)
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

    companion object {
        private const val ROOT_ID = "root"
        private const val ACTIVITY_REQUEST_CODE = 0x213
        private const val ACTIVITY_NO_FLAGS = 0
    }
}
