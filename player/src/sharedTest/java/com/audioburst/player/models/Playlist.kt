package com.audioburst.player.models

import com.audioburst.library.models.Burst
import com.audioburst.library.models.PlayerAction
import com.audioburst.library.models.PlayerSessionId
import com.audioburst.library.models.Playlist

internal fun playlistOf(
    id: String = "",
    name: String = "",
    query: String = "",
    bursts: List<Burst> = emptyList(),
    playerSessionId: PlayerSessionId = PlayerSessionId(""),
    playerAction: PlayerAction = playerActionOf(),
): Playlist =
    Playlist(
        id = id,
        name = name,
        query = query,
        bursts = bursts,
        playerSessionId = playerSessionId,
        playerAction = playerAction,
    )

internal fun playerActionOf(
    type: PlayerAction.Type = PlayerAction.Type.Personalized,
    value: String = ""
): PlayerAction =
    PlayerAction(
        type = type,
        value = value,
    )