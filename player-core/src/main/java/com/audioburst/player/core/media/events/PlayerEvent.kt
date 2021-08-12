package com.audioburst.player.core.media.events

internal sealed class PlayerEvent {

    data class IsPlayingChanged(val isPlaying: Boolean) : PlayerEvent()

    sealed class Error : PlayerEvent() {
        class UnsupportedUrlException(val url: String) : Error()
    }

    data class PlayerStateChanged(val playWhenReady: Boolean, val playbackState: Int) : PlayerEvent()
}