package com.audioburst.player.utils

import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.media.events.PlayerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ListenedMediaObserverTest {

    private fun testListenedMediaObserverWithParams(
        totalPlayTime: Duration,
        minimumListenedTimeForMedia: Duration?,
        expectedWasListenerCalled: Boolean,
    ) = runBlocking {
        // GIVEN
        val channel = Channel<PlayerEvent>()
        val scope = CoroutineScope(coroutineContext)
        val listenedMediaObserver = TimeBasedListenedMediaObserver(
            scope = this,
            playingAwareTimerCreator = PlayingAwareTimer.Creator(
                scope = scope,
                playerEventFlow = playerEventFlowOf(channel.consumeAsFlow())
            ),
            mediaTotalPlayTimeProvider = mediaTotalPlayTimeProviderOf(totalPlayTime = totalPlayTime)
        )

        var wasListenerCalled = false
        listenedMediaObserver.onListenedObserver = {
            wasListenerCalled = true
        }

        // WHEN
        listenedMediaObserver.setMinimumListenedTimeForMedia(minimumListenedTimeForMedia)
        channel.send(PlayerEvent.IsPlayingChanged(isPlaying = true))

        delay(2000)

        channel.send(PlayerEvent.IsPlayingChanged(isPlaying = false))

        // THEN
        assert(expectedWasListenerCalled == wasListenerCalled)
        channel.close()
        scope.coroutineContext.cancelChildren()
    }

    @Test
    fun `test if callback is called when minimum listened time is lower than total play time`() = runBlocking {
        testListenedMediaObserverWithParams(
            totalPlayTime = 3.0.toDuration(DurationUnit.Seconds),
            minimumListenedTimeForMedia = 2.0.toDuration(DurationUnit.Seconds),
            expectedWasListenerCalled = true,
        )
    }

    @Test
    fun `test if callback is not called when minimum listened time is bigger than total play time`() = runBlocking {
        testListenedMediaObserverWithParams(
            totalPlayTime = 2.0.toDuration(DurationUnit.Seconds),
            minimumListenedTimeForMedia = 3.0.toDuration(DurationUnit.Seconds),
            expectedWasListenerCalled = false,
        )
    }

    @Test
    fun `test if callback is not called when minimum listened time is null`() = runBlocking {
        testListenedMediaObserverWithParams(
            totalPlayTime = 2.0.toDuration(DurationUnit.Seconds),
            minimumListenedTimeForMedia = null,
            expectedWasListenerCalled = false,
        )
    }
}

internal fun mediaTotalPlayTimeProviderOf(totalPlayTime: Duration): MediaTotalPlayTimeProvider =
    object : MediaTotalPlayTimeProvider {
        override val totalPlayTime: Duration
            get() = totalPlayTime

        override fun clear() = Unit
    }