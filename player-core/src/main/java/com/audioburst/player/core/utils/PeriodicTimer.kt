package com.audioburst.player.core.utils

import com.audioburst.library.models.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class PeriodicTimer(
    private val interval: Duration,
    private val scope: CoroutineScope,
) {

    private var currentTimerJob: Job? = null
    private var isRunning: Boolean = false
    private val sharedFlow = MutableSharedFlow<Tick>()
    val timer: Flow<Tick> = sharedFlow.asSharedFlow()

    fun start() {
        if (!isRunning) {
            currentTimerJob = scope.launch {
                isRunning = true
                while (isRunning) {
                    delay(interval.milliseconds.toLong())
                    sharedFlow.emit(Tick)
                }
            }
        }
    }

    fun pause() {
        isRunning = false
        currentTimerJob?.cancel()
    }

    object Tick
}
