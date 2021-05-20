package com.audioburst.player.core.utils

import com.audioburst.library.models.Duration
import com.audioburst.player.core.media.events.PlayerEvent
import com.audioburst.player.core.media.events.PlayerEventFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

internal class PlayingAwareTimer private constructor(
    interval: Duration,
    scope: CoroutineScope,
    playerEventFlow: PlayerEventFlow,
) {

    private val periodicTimer = PeriodicTimer(interval, scope)
    val timer: Flow<PeriodicTimer.Tick> = periodicTimer.timer

    init {
        playerEventFlow()
            .filterIsInstance<PlayerEvent.IsPlayingChanged>()
            .debounce(timeoutMillis = 200)
            .onEach {
                if (it.isPlaying) {
                    periodicTimer.start()
                } else {
                    periodicTimer.pause()
                }
            }
            .launchIn(scope)
    }

    class Creator(
        private val scope: CoroutineScope,
        private val playerEventFlow: PlayerEventFlow,
    ) {
        fun create(interval: Duration): PlayingAwareTimer =
            PlayingAwareTimer(
                interval = interval,
                scope = scope,
                playerEventFlow = playerEventFlow,
            )
    }
}