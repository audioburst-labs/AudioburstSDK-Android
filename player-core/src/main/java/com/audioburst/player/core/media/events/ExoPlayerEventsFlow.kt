package com.audioburst.player.core.media.events

import com.audioburst.player.core.media.UnsupportedUrlException
import com.audioburst.player.core.models.AppDispatchers
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

                override fun onPlaybackStateChanged(state: Int) {
                    offer(
                        PlayerEvent.PlayerStateChanged(
                            playWhenReady = exoPlayer.playWhenReady,
                            playbackState = state,
                        )
                    )
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    offer(
                        PlayerEvent.PlayerStateChanged(
                            playWhenReady = playWhenReady,
                            playbackState = exoPlayer.playbackState,
                        )
                    )
                }
            }

            exoPlayer.addListener(listener)
            awaitClose { exoPlayer.removeListener(listener) }
        }.flowOn(appDispatchers.main)
}

