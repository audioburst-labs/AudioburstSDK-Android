package com.audioburst.player.core.media.mappers

import com.audioburst.library.models.Burst
import com.audioburst.library.models.BurstSource
import com.audioburst.player.core.models.BurstIdUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes

internal class BurstToMediaItemMapper {

    fun map(from: Burst): MediaItem =
        map(
            burst = from,
            id = from.id,
            identity = from.identity,
        )

    fun mapWithSource(from: Burst): MediaItem? =
        from.source.identity?.let { identity ->
            map(
                burst = from,
                id = from.id,
                identity = identity,
            )
        }

    private fun map(burst: Burst, id: String, identity: Identity): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(identity.value)
            .setMimeType(identity.mimeType)
            .setTag(burst)
            .build()

    private val BurstSource.identity: Identity?
        get() = audioUrl?.let(Identity.Companion::mp3)

    private val Burst.identity: Identity
        get() = if (isAdAvailable) {
            Identity.mp3(value = BurstIdUri(burst = this).uri.toString())
        } else {
            streamUrl?.let(Identity.Companion::stream) ?: audioUrl.let(Identity.Companion::mp3)
        }

    private data class Identity(
        val value: String,
        val mimeType: String,
    ) {
        companion object {
            fun mp3(value: String): Identity =
                Identity(
                    value = value,
                    mimeType = MimeTypes.AUDIO_MPEG,
                )

            fun stream(value: String): Identity =
                Identity(
                    value = value,
                    mimeType = MimeTypes.APPLICATION_M3U8,
                )
        }
    }
}