package com.audioburst.player.utils

import com.audioburst.player.models.playlistOf
import org.junit.Test

internal class InMemoryCurrentPlaylistCacheTest {

    @Test
    fun `test if initial value is null`() {
        // GIVEN
        val playlistCache = InMemoryCurrentPlaylistCache()

        // WHEN
        val currentPlaylist = playlistCache.currentPlaylist.value

        // THEN
        assert(currentPlaylist == null)
    }

    @Test
    fun `test if setting new value works as expected`() {
        // GIVEN
        val playlistCache = InMemoryCurrentPlaylistCache()
        val playlist = playlistOf()

        // WHEN
        playlistCache.setCurrentPlaylist(playlist)
        val currentPlaylist = playlistCache.currentPlaylist.value

        // THEN
        assert(currentPlaylist == playlist)
    }
}