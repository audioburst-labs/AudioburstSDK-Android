package com.audioburst.player.utils

import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.analytics.PlaybackStatsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ListenedMediaObserver(
    scope: CoroutineScope,
    playingAwareTimerCreator: PlayingAwareTimer.Creator,
    private val analyticsCollector: AnalyticsCollector,
) {

    var onListenedObserver: (() -> Unit)? = null
    private val playingAwareTimer = playingAwareTimerCreator.create(refreshInterval)
    private var minimumListenedTimeForMedia: Duration? = null
    private val statsListener = PlaybackStatsListener(false, null)

    init {
        analyticsCollector.addListener(statsListener)
        playingAwareTimer.timer
            .onEach { onTick() }
            .launchIn(scope)
    }

    private fun onTick() {
        val minimumListenedTimeMs = minimumListenedTimeForMedia?.milliseconds?.toLong() ?: return
        val totalPlayTimeMs = statsListener.playbackStats?.totalPlayTimeMs ?: return
        if (totalPlayTimeMs >= minimumListenedTimeMs) {
            onListenedObserver?.invoke()
            minimumListenedTimeForMedia = null
        }
    }

    fun setMinimumListenedTimeForMedia(minimumListenedTimeForMedia: Duration?) {
        this.minimumListenedTimeForMedia = minimumListenedTimeForMedia
    }

    fun finish() {
        analyticsCollector.removeListener(statsListener)
        onListenedObserver = null
    }

    companion object {
        private val refreshInterval = 1.0.toDuration(DurationUnit.Seconds)
    }
}