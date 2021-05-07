package com.audioburst.player.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal interface AdStateProvider {

    fun onNewMedia(media: BurstPlayer.NowPlaying.Media, currentPlayBackPosition: Long)

    val adState: StateFlow<BurstPlayer.AdState>
}
// TODO: To be implemented later
internal class NoOpAdStateProvider : AdStateProvider {
    override fun onNewMedia(media: BurstPlayer.NowPlaying.Media, currentPlayBackPosition: Long) {
    }

    override val adState: StateFlow<BurstPlayer.AdState>
        get() = MutableStateFlow(BurstPlayer.AdState())
}