package com.audioburst.player.utils

import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.analytics.PlaybackStatsListener

internal interface MediaTotalPlayTimeProvider {

    val totalPlayTime: Duration

    fun clear()
}

internal class AnalyticCollectorMediaTotalPlayTimeProvider(
    private val analyticsCollector: AnalyticsCollector
) : MediaTotalPlayTimeProvider {

    private val statsListener = PlaybackStatsListener(false, null)
    override val totalPlayTime: Duration
        get() = statsListener.playbackStats?.totalPlayTimeMs.let {
            (it?.toDouble() ?: 0.0).toDuration(DurationUnit.Milliseconds)
        }

    init {
        analyticsCollector.addListener(statsListener)
    }

    override fun clear() {
        analyticsCollector.removeListener(statsListener)
    }
}