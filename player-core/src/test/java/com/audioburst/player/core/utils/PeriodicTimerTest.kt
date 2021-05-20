package com.audioburst.player.core.utils

import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class PeriodicTimerTest {

    @Test
    fun `test timer`() = runBlocking {
        // GIVEN
        val intervalMillis = 100.0
        val times = 10
        val interval = intervalMillis.toDuration(DurationUnit.Milliseconds)

        // WHEN
        val timer = PeriodicTimer(interval = interval, scope = this)
        timer.start()
        val results = mutableListOf<PeriodicTimer.Tick>()
        val job = launch {
            timer.timer.toList(results)
        }
        delay(intervalMillis.toLong() * times)
        timer.pause()

        // THEN
        assert(results.size == times - 1)
        job.cancel()
    }
}