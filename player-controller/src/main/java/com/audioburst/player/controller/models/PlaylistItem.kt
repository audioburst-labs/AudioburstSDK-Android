package com.audioburst.player.controller.models

public data class PlaylistItem(
    val id: String,
    val title: String?,
    val subtitle: String?,
    val rightText: RightText,
    val theme: PlayerTheme? = null,
    val isPlaying: Boolean = false,
    val color: Color = Color.DEFAULT,
    val callback: (UiEvent) -> Unit,
) {

    public enum class RightText {
        Empty, Time, Listened, Playing
    }
}