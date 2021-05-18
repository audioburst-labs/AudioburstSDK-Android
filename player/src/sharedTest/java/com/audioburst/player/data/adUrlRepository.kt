package com.audioburst.player.data

import com.audioburst.library.models.Burst
import com.audioburst.library.models.LibraryError
import com.audioburst.library.models.Result

internal fun repositoryOf(
    getAdUrl: Result<String> = Result.Error(LibraryError.Unexpected),
): AdUrlRepository = object : AdUrlRepository {
    override suspend fun getAdUrl(burst: Burst): Result<String> = getAdUrl
}