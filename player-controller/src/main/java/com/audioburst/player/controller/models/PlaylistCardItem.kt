package com.audioburst.player.controller.models

public data class PlaylistCardItem(
    val id: String,
    val bottomSubtitle: String?,
    val bottomTitle: String,
    val timeText: String?,
    val title: String,
    val gradient: Gradient,
    val isLoadingFullShow: Boolean,
    val playerStyle: PlayerStyle,
    val callback: (UiEvent) -> Unit,
)