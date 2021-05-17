package com.audioburst.player.utils

import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.media.events.PlayerEvent
import com.audioburst.player.media.events.PlayerEventFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import org.junit.Test

internal class PlayingAwareTimerTest {

    @Test
    fun `test if timer starts and stops in between isPlaying=true and isPlaying=false`() = runBlocking {
        // GIVEN
        val intervalMillis = 1000.0
        val times = 5
        val interval = intervalMillis.toDuration(DurationUnit.Milliseconds)

        val scope = CoroutineScope(coroutineContext)
        val channel = Channel<PlayerEvent>()
        val timer = PlayingAwareTimer.Creator(
            scope = scope,
            playerEventFlow = playerEventFlowOf(channel.consumeAsFlow())
        ).create(interval)

        // WHEN
        channel.send(PlayerEvent.IsPlayingChanged(isPlaying = true))

        val results = mutableListOf<PeriodicTimer.Tick>()
        val job = launch {
            timer.timer.toList(results)
        }

        delay(intervalMillis.toLong() * times)

        channel.send(PlayerEvent.IsPlayingChanged(isPlaying = false))

        // THEN
        assert(results.size == times - 1)
        job.cancel()
        scope.coroutineContext.cancelChildren()
    }
}

internal fun playerEventFlowOf(flow: Flow<PlayerEvent>): PlayerEventFlow =
    object : PlayerEventFlow {
        override fun invoke(): Flow<PlayerEvent> = flow
    }