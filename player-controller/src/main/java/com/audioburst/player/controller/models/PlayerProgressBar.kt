package com.audioburst.player.controller.models

public data class PlayerProgressBar(
    val totalTime: String = "",
    val timeElapsed: String = "",
    val progressText: String = "",
    val isPlaying: Boolean = false,
    val isSeekable: Boolean = false,
    val currentProgress: Int = 0,
    val maxProgress: Int = 0,
    val callback: (UiEvent) -> Unit,
)