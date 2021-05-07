package com.audioburst.player.models

internal sealed class MediaUrl {
    abstract val url: String

    companion object {
        private const val HLS_EXTENSION = ".m3u8"
        private const val PROGRESSIVE_EXTENSION = ".mp3"
        private const val STREAM_EXTENSION = ".ts"

        fun create(url: String): MediaUrl =
            when {
                url.contains(PROGRESSIVE_EXTENSION) -> Progressive(url)
                url.contains(HLS_EXTENSION) -> Hls(url)
                else -> HlsWithAd(url)
            }

        fun isMediaUrl(url: String): Boolean =
            url.contains(PROGRESSIVE_EXTENSION) || url.contains(HLS_EXTENSION) || url.contains(STREAM_EXTENSION)
    }
}

internal data class Progressive(override val url: String) : MediaUrl()

internal data class Hls(override val url: String) : MediaUrl()

internal data class HlsWithAd(override val url: String) : MediaUrl()