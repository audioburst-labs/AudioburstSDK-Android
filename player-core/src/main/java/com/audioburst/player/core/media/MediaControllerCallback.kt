package com.audioburst.player.core.media

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class MediaControllerCallback(
    private val connectionCallback: MediaBrowserConnectionCallback
) : MediaControllerCompat.Callback() {

    private val _playbackState = MutableStateFlow(EMPTY_PLAYBACK_STATE)
    val playbackState = _playbackState.asStateFlow()

    private val _nowPlaying = MutableStateFlow(NOTHING_PLAYING)
    val nowPlaying = _nowPlaying.asStateFlow()

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        _playbackState.value = state ?: EMPTY_PLAYBACK_STATE
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        _nowPlaying.value = metadata ?: NOTHING_PLAYING
    }

    override fun onSessionDestroyed() {
        connectionCallback.onConnectionSuspended()
    }
}

internal val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

internal val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()