package com.audioburst.player.core.interactors

import com.audioburst.library.models.Playlist
import kotlinx.coroutines.flow.Flow

internal interface ObservePlaylist {

    operator fun invoke(): Flow<Playlist?>
}