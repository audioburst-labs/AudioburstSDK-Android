package com.audioburst.player.core.media

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.flow.StateFlow

internal interface MediaPlayer {

    val currentTimeline: List<String>

    val nowPlaying: StateFlow<MediaMetadataCompat>

    val state: StateFlow<PlaybackStateCompat>

    val isPlaying: StateFlow<Boolean>

    val playbackPosition: Long

    fun play(list: List<MediaItem>, shouldUseCache: Boolean, mediaId: String?, playWhenReady: Boolean = true)

    fun playAt(position: Int, time: Long = 0)

    fun addAll(list: List<MediaItem>, shouldUseCache: Boolean)

    fun replaceAt(media: MediaItem, position: Int, seekTo: Long)

    fun add(list: List<MediaItem>, shouldUseCache: Boolean)

    fun removeAt(position: Int): Boolean

    fun insert(position: Int, item: MediaItem, shouldUseCache: Boolean = true)

    fun play()

    fun pause()

    fun skipToNext()

    fun skipToPrevious()

    fun seekTo(position: Long)

    fun prepareOnIdle()

    fun stopPlayback()
}