package com.audioburst.player.media

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.audioburst.player.extensions.tags
import com.audioburst.player.media.events.PlayerEvent
import com.audioburst.player.media.events.PlayerEventFlow
import com.audioburst.player.utils.Logger
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.IllegalSeekPositionException
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

internal class ExoMediaPlayer(
    private val scope: CoroutineScope,
    private val exoPlayer: ExoPlayer,
    private val burstDownloader: BurstDownloader,
    private val mediaControllerCallback: MediaControllerCallback,
    private val playerEventFlow: PlayerEventFlow,
) : MediaPlayer {

    override val currentTimeline: List<String>
        get() = exoPlayer
            .currentTimeline
            .tags(exoPlayer.shuffleModeEnabled)

    override val nowPlaying: StateFlow<MediaMetadataCompat>
        get() = mediaControllerCallback.nowPlaying

    override val state: StateFlow<PlaybackStateCompat>
        get() = mediaControllerCallback.playbackState

    override val nowPlayingTags: StateFlow<List<String>>
        get() = playerEventFlow()
            .filterIsInstance<PlayerEvent.TimelineChanged>()
            .map { it.timeline }
            .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    override val isPlaying: StateFlow<Boolean>
        get() = playerEventFlow()
            .filterIsInstance<PlayerEvent.IsPlayingChanged>()
            .map { it.isPlaying }
            .stateIn(scope, SharingStarted.WhileSubscribed(), exoPlayer.isPlaying)

    override val playbackPosition: Long
        get() = exoPlayer.currentPosition

    override fun play(list: List<MediaItem>, shouldUseCache: Boolean, mediaId: String?, playWhenReady: Boolean) {
        if (list.isEmpty()) {
            return
        }

        val initialWindowIndex = if (mediaId == null) 0 else list.map { it.mediaId }.indexOf(mediaId)

        if (shouldUseCache) {
            burstDownloader.startDownloading(list, initialWindowIndex)
        }

        if (initialWindowIndex == -1) {
            return
        }

        with(exoPlayer) {
            clearMediaItems()
            addAll(list, shouldUseCache)
            this.playWhenReady = playWhenReady
            prepare()
            seekTo(initialWindowIndex, 0)
        }
    }

    override fun playAt(position: Int, time: Long) {
        try {
            exoPlayer.seekTo(position, time)
        } catch (exception: IllegalSeekPositionException) {
            Logger.logException(exception)
            exoPlayer.seekTo(0, time)
        }
    }

    override fun addAll(list: List<MediaItem>, shouldUseCache: Boolean) {
        val timeline = currentTimeline
        val mediasToAdd = list.filter { media -> media.mediaId.let { !timeline.contains(it) } }
        exoPlayer.addMediaItems(mediasToAdd)
        if (shouldUseCache) {
            burstDownloader.addToDownload(mediasToAdd)
        }
    }

    override fun replaceAt(media: MediaItem, position: Int, seekTo: Long) {
        try {
            with(exoPlayer) {
                removeMediaItem(position)
                addMediaItem(position, media)
                seekTo(position, seekTo)
            }
        } catch (exception: IndexOutOfBoundsException) {
            Logger.logException(exception)
        }
    }

    override fun add(list: List<MediaItem>, shouldUseCache: Boolean) {
        addAll(list, shouldUseCache)
    }

    override fun removeAt(position: Int): Boolean =
        try {
            exoPlayer.removeMediaItem(position)
            true
        } catch (exception: IndexOutOfBoundsException) {
            Logger.logException(exception)
            false
        }

    override fun insert(position: Int, item: MediaItem, shouldUseCache: Boolean) {
        try {
            if (shouldUseCache) {
                burstDownloader.addToDownload(item)
            }
            exoPlayer.addMediaItem(position, item)
        } catch (exception: IndexOutOfBoundsException) {
            Logger.logException(exception)
        }
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun skipToNext() {
        exoPlayer.next()
    }

    override fun skipToPrevious() {
        exoPlayer.previous()
    }

    override fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    override fun prepareOnIdle() {
        if (exoPlayer.playbackState == ExoPlayer.STATE_IDLE) {
            exoPlayer.prepare()
        }
    }

    override fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }
}
