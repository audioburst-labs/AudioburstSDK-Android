package com.audioburst.player.controller.models

internal data class PlayerStyle(
    val playerTheme: PlayerTheme,
    val colorAccent: Color = Color.DEFAULT,
    val gradient: Gradient = Gradient.Horizontal.DEFAULT,
)
