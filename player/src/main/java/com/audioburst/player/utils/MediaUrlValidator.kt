package com.audioburst.player.utils

internal interface MediaUrlValidator {

    fun isValid(url: String): Boolean
}

internal class ExtensionBasedMediaUrlValidator: MediaUrlValidator {

    override fun isValid(url: String): Boolean =
        url.contains(PROGRESSIVE_EXTENSION) || url.contains(HLS_EXTENSION) || url.contains(STREAM_EXTENSION)

    companion object {
        private const val HLS_EXTENSION = ".m3u8"
        private const val PROGRESSIVE_EXTENSION = ".mp3"
        private const val STREAM_EXTENSION = ".ts"
    }
}