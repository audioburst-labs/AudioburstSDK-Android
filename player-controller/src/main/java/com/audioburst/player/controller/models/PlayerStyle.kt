package com.audioburst.player.controller.models

public data class PlayerStyle(
    val playerTheme: PlayerTheme,
    val colorAccent: Color = Color.DEFAULT,
    val gradient: Gradient = Gradient.Horizontal.DEFAULT,
    val gradientTint: Color = Color.WHITE,
)
