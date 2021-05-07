package com.audioburst.player.media

import android.support.v4.media.MediaBrowserCompat
import kotlinx.coroutines.flow.MutableStateFlow

internal enum class MediaBrowserConnectionState {
    Connected, Disconnected, Failed, Suspended
}

internal class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

    private val _state: MutableStateFlow<MediaBrowserConnectionState> = MutableStateFlow(MediaBrowserConnectionState.Disconnected)

    override fun onConnected() {
        _state.value = MediaBrowserConnectionState.Connected
    }

    override fun onConnectionFailed() {
        _state.value = MediaBrowserConnectionState.Failed
    }

    override fun onConnectionSuspended() {
        _state.value = MediaBrowserConnectionState.Suspended
    }
}