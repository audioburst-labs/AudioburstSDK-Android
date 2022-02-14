package com.audioburst.player.core.models

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
    intent: Playlist.Intent? = null,
): Playlist =
    Playlist(
        id = id,
        name = name,
        query = query,
        bursts = bursts,
        playerSessionId = playerSessionId,
        playerAction = playerAction,
        intent = intent,
    )

internal fun playerActionOf(
    type: PlayerAction.Type = PlayerAction.Type.Personalized,
    value: String = ""
): PlayerAction =
    PlayerAction(
        type = type,
        value = value,
    )