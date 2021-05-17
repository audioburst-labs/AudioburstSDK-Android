package com.audioburst.player.media

import android.net.Uri
import com.audioburst.library.models.Burst
import com.audioburst.library.models.Playlist
import com.audioburst.player.interactors.GetAdvertisementUrl
import com.audioburst.player.models.burstOf
import com.audioburst.player.models.playlistOf
import com.audioburst.player.utils.CurrentPlaylistCache
import com.google.android.exoplayer2.upstream.DataSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test

internal class AdUriResolverTest {

    @Test
    fun testIfTheSameDataSpecIsReturnedWhenIsUriValidReturnsTrue() {
        // GIVEN
        val dataSpec = DataSpec(Uri.EMPTY)

        // WHEN
        val resolved = adUriResolverOf().resolveDataSpec(dataSpec)

        // THEN
        assert(dataSpec == resolved)
    }

    @Test(expected = UnsupportedUrlException::class)
    fun testIfUnsupportedUrlExceptionIsThrownWhenUriIsBurstIdAndPlaylistIsNull() {
        // GIVEN
        val uri = Uri.parse("burstId://id")
        val dataSpec = DataSpec(uri)

        // WHEN
        adUriResolverOf(
            currentPlaylistCache = currentPlaylistCacheOf(MutableStateFlow(null))
        ).resolveDataSpec(dataSpec)
    }

    @Test(expected = UnsupportedUrlException::class)
    fun testIfUnsupportedUrlExceptionIsThrownWhenUriIsBurstIdPlaylistIsNotNullButUriDoesntContainBurstId() {
        // GIVEN
        val uri = Uri.parse("burstId://id")
        val dataSpec = DataSpec(uri)

        // WHEN
        adUriResolverOf(
            currentPlaylistCache = currentPlaylistCacheOf(MutableStateFlow(playlistOf()))
        ).resolveDataSpec(dataSpec)
    }

    @Test(expected = UnsupportedUrlException::class)
    fun testIfUnsupportedUrlExceptionIsThrownUriIsBurstIdAndPlaylistIsNotNullUriContainsBurstIdButBurstIsNotOnPlaylist() {
        // GIVEN
        val id = "id"
        val uri = Uri.parse("burstId://$id")
        val dataSpec = DataSpec(uri)

        // WHEN
        adUriResolverOf(
            currentPlaylistCache = currentPlaylistCacheOf(MutableStateFlow(playlistOf(bursts = emptyList())))
        ).resolveDataSpec(dataSpec)
    }

    @Test
    fun testIfResolvedUrlIsReturnedUriIsBurstIdAndPlaylistIsNotNullUriContainsBurstIdAndBurstIsOnPlaylist() {
        // GIVEN
        val id = "id"
        val uri = Uri.parse("burstId://$id")
        val dataSpec = DataSpec(uri)
        val expectedUri = Uri.parse("https://google.com")

        // WHEN
        val resolvedUri = adUriResolverOf(
            currentPlaylistCache = currentPlaylistCacheOf(MutableStateFlow(playlistOf(bursts = listOf(burstOf(id = id))))),
            getAdvertisementUrl = getAdvertisementUrlOf(url = expectedUri.toString())
        ).resolveDataSpec(dataSpec)

        // THEN
        assert(resolvedUri.uri.toString() == expectedUri.toString())
    }
}

internal fun adUriResolverOf(
    currentPlaylistCache: CurrentPlaylistCache = currentPlaylistCacheOf(),
    getAdvertisementUrl: GetAdvertisementUrl = getAdvertisementUrlOf(),
): AdUriResolver =
    AdUriResolver(
        currentPlaylistCache = currentPlaylistCache,
        getAdvertisementUrl = getAdvertisementUrl,
    )

internal fun getAdvertisementUrlOf(url: String = ""): GetAdvertisementUrl =
    object : GetAdvertisementUrl {
        override suspend fun invoke(burst: Burst): String = url
    }

internal fun currentPlaylistCacheOf(currentPlaylist: StateFlow<Playlist?> = MutableStateFlow(null)): CurrentPlaylistCache =
    object : CurrentPlaylistCache {
        override val currentPlaylist: StateFlow<Playlist?> = currentPlaylist
    }