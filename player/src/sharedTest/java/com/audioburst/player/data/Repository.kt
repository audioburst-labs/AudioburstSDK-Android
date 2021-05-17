package com.audioburst.player.data

import com.audioburst.library.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal fun repositoryOf(
    getAdUrl: Result<String> = Result.Error(LibraryError.Unexpected),
    getPersonalPlaylist: Flow<Result<PendingPlaylist>> = flowOf(Result.Error(LibraryError.Unexpected)),
    getPlaylistWithPlaylistInfo: Result<Playlist> = Result.Error(LibraryError.Unexpected),
    getPlaylistWithByteArray: Result<Playlist> = Result.Error(LibraryError.Unexpected),
    getPlaylists: Result<List<PlaylistInfo>> = Result.Error(LibraryError.Unexpected),
): Repository = object : Repository {
    override suspend fun getAdUrl(burst: Burst): Result<String> = getAdUrl
    override suspend fun getPersonalPlaylist(): Flow<Result<PendingPlaylist>> = getPersonalPlaylist
    override suspend fun getPlaylist(playlistInfo: PlaylistInfo): Result<Playlist> = getPlaylistWithPlaylistInfo
    override suspend fun getPlaylist(byteArray: ByteArray): Result<Playlist> = getPlaylistWithByteArray
    override suspend fun getPlaylists(): Result<List<PlaylistInfo>> = getPlaylists
}