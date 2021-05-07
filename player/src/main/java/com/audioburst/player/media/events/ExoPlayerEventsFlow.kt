package com.audioburst.player.media.events

import com.audioburst.player.extensions.tags
import com.audioburst.player.media.UnsupportedUrlException
import com.audioburst.player.models.AppDispatchers
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

internal class PlayerEventFlow(
    private val exoPlayer: ExoPlayer,
    private val appDispatchers: AppDispatchers,
) {

    operator fun invoke(): Flow<PlayerEvent> =
        callbackFlow {
            val listener = object : Player.EventListener {
                override fun onPositionDiscontinuity(reason: Int) {
                    offer(
                        PlayerEvent.PositionDiscontinuity(
                            eventTime = exoPlayer.currentPosition,
                            reason = PlayerEvent.PositionDiscontinuity.Reason.create(reason)
                        )
                    )
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    super.onTimelineChanged(timeline, reason)
                    offer(
                        PlayerEvent.TimelineChanged(
                            timeline = if (timeline.isEmpty) emptyList() else timeline.tags(exoPlayer.shuffleModeEnabled),
                            reason = PlayerEvent.TimelineChanged.Reason.create(reason)
                        )
                    )
                }

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

