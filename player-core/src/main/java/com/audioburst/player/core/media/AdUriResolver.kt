package com.audioburst.player.core.media

import android.net.Uri
import com.audioburst.player.core.interactors.GetAdvertisementUrl
import com.audioburst.player.core.models.BurstIdUri
import com.audioburst.player.core.utils.CurrentPlaylistCache
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import kotlinx.coroutines.runBlocking

internal class AdUriResolver(
    private val currentPlaylistCache: CurrentPlaylistCache,
    private val getAdvertisementUrl: GetAdvertisementUrl,
) : ResolvingDataSource.Resolver {

    override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
        val url = dataSpec.uri.toString()
        val id = BurstIdUri.burstIdFrom(dataSpec.uri)
        return if (id != null) {
            runBlocking {
                val currentPlaylist = currentPlaylistCache.currentPlaylist.value ?: throw UnsupportedUrlException(url)
                val burst = currentPlaylist.bursts.firstOrNull { it.id == id } ?: throw UnsupportedUrlException(url)
                val uri = getAdvertisementUrl(burst)
                dataSpec.withUri(Uri.parse(uri))
            }
        } else {
            dataSpec
        }
    }
}

internal class UnsupportedUrlException(val url: String) : Exception("Unsupported url passed to the player: $url")