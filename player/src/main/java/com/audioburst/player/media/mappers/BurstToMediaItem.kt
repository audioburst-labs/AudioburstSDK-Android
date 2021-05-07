package com.audioburst.player.media.mappers

import com.audioburst.library.models.Burst
import com.audioburst.player.models.*

internal class BurstToMediaItem {

    fun map(from: Burst) = MediaItem(
        id = from.id,
        title = from.title,
        displayTitle = from.title,
        displaySubtitle = from.sourceName,
        mediaUri = from.mediaUrl(),
        isPlayable = true,
    )

    private fun Burst.mediaUrl(): MediaUrl =
        if (isAdAvailable) {
            HlsWithAd("")
        } else {
            streamUrl?.let { Hls(it) } ?: Progressive(audioUrl)
        }
}
