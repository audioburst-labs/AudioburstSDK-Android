package com.audioburst.player.media

import android.net.Uri
import com.audioburst.player.di.provider.Provider
import com.audioburst.player.interactors.GetAdvertisementUrl
import com.audioburst.player.utils.MediaUrlValidator
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import kotlinx.coroutines.runBlocking

internal class AdUriResolver(
    private val burstPlayerProvider: Provider<BurstPlayer>,
    private val mediaUrlValidator: MediaUrlValidator,
    private val getAdvertisementUrl: GetAdvertisementUrl,
) : ResolvingDataSource.Resolver {

    override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
        val url = dataSpec.uri.toString()
        return if (!mediaUrlValidator.isValid(url)) {
            runBlocking {
                val uri = getAdvertisementUrl(url, burstPlayerProvider.get().currentPlaylist.value!!) ?: throw UnsupportedUrlException(url)
                dataSpec.withUri(Uri.parse(uri))
            }
        } else {
            dataSpec
        }
    }
}

internal class UnsupportedUrlException(val url: String) : Exception("Unsupported url passed to the player: $url")