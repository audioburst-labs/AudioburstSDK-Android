package com.audioburst.player.data

import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.Burst
import com.audioburst.library.models.Result

internal interface AdUrlRepository {

    suspend fun getAdUrl(burst: Burst): Result<String>
}

internal class AudioburstLibraryRepository(
    private val audioburstLibrary: AudioburstLibrary
) : AdUrlRepository {

    override suspend fun getAdUrl(burst: Burst): Result<String> =
        audioburstLibrary.getAdUrl(burst)
}