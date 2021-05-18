package com.audioburst.player.interactors

import com.audioburst.library.models.LibraryError
import com.audioburst.library.models.Result
import com.audioburst.player.data.AdUrlRepository
import com.audioburst.player.data.repositoryOf
import com.audioburst.player.models.burstOf
import com.audioburst.player.utils.AdUrlCache
import com.audioburst.player.utils.InMemoryAdUrlCache
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class GetAdvertisementUrlInteractorTest {

    private fun interactor(
        getAdUrlReturns: Result<String>,
        adUrlCache: AdUrlCache,
    ): GetAdvertisementUrlInteractor =
        getAdvertisementUrlInteractorOf(
            adUrlRepository = repositoryOf(getAdUrl = getAdUrlReturns),
            adUrlCache = adUrlCache,
        )

    @Test
    fun `test if adUrl is returned and saved in cache when repository returns Data`() = runBlocking {
        // GIVEN
        val adUrl = "adUrl"
        val getAdUrlReturns = Result.Data(adUrl)
        val adUrlCache = InMemoryAdUrlCache()
        val burst = burstOf()

        // WHEN
        val result = interactor(getAdUrlReturns = getAdUrlReturns, adUrlCache = adUrlCache).invoke(burst)

        // THEN
        require(result == adUrl)
        require(adUrlCache.get(burst) == adUrl)
    }

    @Test
    fun `test if audioUrl is returned and is not saved in cache when repository returns Error`() = runBlocking {
        // GIVEN
        val audioUrl = "audioUrl"
        val getAdUrlReturns = Result.Error(LibraryError.Unexpected)
        val adUrlCache = InMemoryAdUrlCache()
        val burst = burstOf(audioUrl = audioUrl)

        // WHEN
        val result = interactor(getAdUrlReturns = getAdUrlReturns, adUrlCache = adUrlCache).invoke(burst)

        // THEN
        require(result == audioUrl)
        require(adUrlCache.get(burst) == null)
    }
}

internal fun getAdvertisementUrlInteractorOf(
    adUrlCache: AdUrlCache,
    adUrlRepository: AdUrlRepository,
): GetAdvertisementUrlInteractor =
    GetAdvertisementUrlInteractor(
        adUrlCache = adUrlCache,
        adUrlRepository = adUrlRepository,
    )