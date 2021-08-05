package com.audioburst.player.controller.models

public data class AdOverlayState(
    val adTimeLeft: Int = 0,
    val buttonText: String = "",
    val isVisible: Boolean = false,
    val isSkipButtonVisible: Boolean = false,
    val progress: Int = 0,
    val theme: PlayerTheme? = null,
)