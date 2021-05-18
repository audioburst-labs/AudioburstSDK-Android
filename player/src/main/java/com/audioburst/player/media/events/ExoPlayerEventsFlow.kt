package com.audioburst.player.media.events

import com.audioburst.player.media.UnsupportedUrlException
import com.audioburst.player.models.AppDispatchers
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

internal interface PlayerEventFlow {

    operator fun invoke(): Flow<PlayerEvent>
}

internal class ExoPlayerEventsFlow(
    private val exoPlayer: ExoPlayer,
    private val appDispatchers: AppDispatchers,
): PlayerEventFlow {

    override operator fun invoke(): Flow<PlayerEvent> =
        callbackFlow {
            val listener = object : Player.EventListener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    offer(PlayerEvent.IsPlayingChanged(isPlaying = isPlaying))
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    val cause = error.sourceException.cause
                    if (cause is UnsupportedUrlException) {
                        offer(PlayerEvent.Error.UnsupportedUrlException(cause.url))
                    }
                }
            }

            exoPlayer.addListener(listener)
            awaitClose { exoPlayer.removeListener(listener) }
        }.flowOn(appDispatchers.main)
}

