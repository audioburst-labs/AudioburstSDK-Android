package com.audioburst.player.core.utils

import com.audioburst.library.models.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface CurrentPlaylistCache {

    val currentPlaylist: StateFlow<Playlist?>
}

internal interface CurrentPlaylistCacheSetter {

    fun setCurrentPlaylist(playlist: Playlist)
}

internal class InMemoryCurrentPlaylistCache : CurrentPlaylistCache, CurrentPlaylistCacheSetter {

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    override val currentPlaylist: StateFlow<Playlist?>
        get() = _currentPlaylist.asStateFlow()

    override fun setCurrentPlaylist(playlist: Playlist) {
        _currentPlaylist.value = playlist
    }
}