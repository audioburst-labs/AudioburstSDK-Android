package com.audioburst.player.controller.models

public data class PlaylistItem(
    val id: String,
    val title: String?,
    val subtitle: String?,
    val rightText: RightText,
    val isPlaying: Boolean = false,
    val playerStyle: PlayerStyle,
    val callback: (UiEvent) -> Unit,
) {

    public enum class RightText {
        Empty, Time, Listened, Playing
    }
}