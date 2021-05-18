package com.audioburst.player.interactors

import com.audioburst.library.models.Burst
import com.audioburst.library.models.Result
import com.audioburst.player.data.AdUrlRepository
import com.audioburst.player.utils.AdUrlCache

internal interface GetAdvertisementUrl {

    suspend operator fun invoke(burst: Burst): String
}

internal class GetAdvertisementUrlInteractor(
    private val adUrlCache: AdUrlCache,
    private val adUrlRepository: AdUrlRepository,
) : GetAdvertisementUrl {

    override suspend fun invoke(burst: Burst): String =
        when (val result = adUrlRepository.getAdUrl(burst)) {
            is Result.Data -> {
                adUrlCache.set(burst, result.value)
                result.value
            }
            is Result.Error -> burst.audioUrl
        }
}