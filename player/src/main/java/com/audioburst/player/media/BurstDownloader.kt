package com.audioburst.player.media

import com.google.android.exoplayer2.MediaItem

internal interface BurstDownloader {

    fun startDownloading(list: List<MediaItem>, startIndex: Int)

    fun addToDownload(media: MediaItem)

    fun addToDownload(medias: List<MediaItem>)
}
// TODO: Burst prefetching will be implemented at the later stage
internal class NoOpBurstDownloader : BurstDownloader {

    override fun startDownloading(list: List<MediaItem>, startIndex: Int) {
    }

    override fun addToDownload(media: MediaItem) {
    }

    override fun addToDownload(medias: List<MediaItem>) {
    }
}