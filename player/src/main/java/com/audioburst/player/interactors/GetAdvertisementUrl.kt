package com.audioburst.player.interactors

import com.audioburst.library.models.Playlist

internal interface GetAdvertisementUrl {

    suspend operator fun invoke(url: String, currentPlaylist: Playlist): String?
}
// TODO: To be implemented later
internal class NoOpGetAdvertisementUrl: GetAdvertisementUrl {

    override suspend fun invoke(url: String, currentPlaylist: Playlist): String? {
        return null
    }
}