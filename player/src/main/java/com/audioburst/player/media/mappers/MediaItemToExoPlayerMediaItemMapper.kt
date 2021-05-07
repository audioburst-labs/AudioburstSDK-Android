package com.audioburst.player.media.mappers

import com.audioburst.player.models.*
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.MediaItem as ExoPlayerMediaItem

internal class MediaItemToExoPlayerMediaItemMapper {

    fun map(from: List<MediaItem>): List<ExoPlayerMediaItem> =
        from.map(this::map)

    fun map(from: MediaItem): ExoPlayerMediaItem =
        ExoPlayerMediaItem.Builder()
            .setMediaId(from.id)
            .setUri(from.mediaUri.url)
            .setMimeType(from.mediaUri.mimeType)
            .setTag(from)
            .build()
}

internal val MediaUrl.mimeType: String
    get() = when (this) {
        is Progressive -> MimeTypes.AUDIO_MPEG
        is Hls -> MimeTypes.APPLICATION_M3U8
        is HlsWithAd -> MimeTypes.APPLICATION_M3U8
    }
