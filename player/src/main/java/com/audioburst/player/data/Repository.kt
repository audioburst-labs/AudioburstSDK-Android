package com.audioburst.player.data

import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.*
import kotlinx.coroutines.flow.Flow

internal interface Repository {

    suspend fun getAdUrl(burst: Burst): Result<String>

    suspend fun getPersonalPlaylist(): Flow<Result<PendingPlaylist>>

    suspend fun getPlaylist(playlistInfo: PlaylistInfo): Result<Playlist>

    suspend fun getPlaylist(byteArray: ByteArray): Result<Playlist>

    suspend fun getPlaylists(): Result<List<PlaylistInfo>>
}

internal class AudioburstLibraryRepository(
    private val audioburstLibrary: AudioburstLibrary
): Repository {

    override suspend fun getAdUrl(burst: Burst): Result<String> =
        audioburstLibrary.getAdUrl(burst)

    override suspend fun getPersonalPlaylist(): Flow<Result<PendingPlaylist>> =
        audioburstLibrary.getPersonalPlaylist()

    override suspend fun getPlaylist(playlistInfo: PlaylistInfo): Result<Playlist> =
        audioburstLibrary.getPlaylist(playlistInfo)

    override suspend fun getPlaylist(byteArray: ByteArray): Result<Playlist> =
        audioburstLibrary.getPlaylist(byteArray)

    override suspend fun getPlaylists(): Result<List<PlaylistInfo>> =
        audioburstLibrary.getPlaylists()
}