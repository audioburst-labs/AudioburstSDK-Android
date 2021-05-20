package com.audioburst.player.core.utils

import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal interface ListenedMediaObserver {

    var onListenedObserver: (() -> Unit)?

    fun setMinimumListenedTimeForMedia(minimumListenedTimeForMedia: Duration?)

    fun finish()
}

internal class TimeBasedListenedMediaObserver(
    scope: CoroutineScope,
    playingAwareTimerCreator: PlayingAwareTimer.Creator,
    private val mediaTotalPlayTimeProvider: MediaTotalPlayTimeProvider,
) : ListenedMediaObserver {

    override var onListenedObserver: (() -> Unit)? = null
    private val playingAwareTimer = playingAwareTimerCreator.create(refreshInterval)
    private var minimumListenedTimeForMedia: Duration? = null

    init {
        playingAwareTimer.timer
            .onEach { onTick() }
            .launchIn(scope)
    }

    private fun onTick() {
        val minimumListenedTimeMs = minimumListenedTimeForMedia?.milliseconds?.toLong() ?: return
        val totalPlayTimeMs = mediaTotalPlayTimeProvider.totalPlayTime.milliseconds
        if (totalPlayTimeMs >= minimumListenedTimeMs) {
            onListenedObserver?.invoke()
            minimumListenedTimeForMedia = null
        }
    }

    override fun setMinimumListenedTimeForMedia(minimumListenedTimeForMedia: Duration?) {
        this.minimumListenedTimeForMedia = minimumListenedTimeForMedia
    }

    override fun finish() {
        mediaTotalPlayTimeProvider.clear()
        onListenedObserver = null
    }

    companion object {
        private val refreshInterval = 1.0.toDuration(DurationUnit.Seconds)
    }
}