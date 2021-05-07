package com.audioburst.player.models

internal data class MediaItem(
    val id: String,
    val title: String,
    val displayTitle: String,
    val displaySubtitle: String,
    val mediaUri: MediaUrl,
    val isPlayable: Boolean,
)