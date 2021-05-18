package com.audioburst.player.media.events

internal sealed class PlayerEvent {

    data class IsPlayingChanged(val isPlaying: Boolean) : PlayerEvent()

    sealed class Error : PlayerEvent() {
        class UnsupportedUrlException(val url: String) : Error()
    }
}