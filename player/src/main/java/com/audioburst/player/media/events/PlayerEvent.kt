package com.audioburst.player.media.events

import com.google.android.exoplayer2.Player

internal sealed class PlayerEvent {
    data class PositionDiscontinuity(val eventTime: Long, val reason: Reason): PlayerEvent() {

        enum class Reason {
            PeriodTransition, Seek, SeekAdjustment, AdInsertion, Internal;

            companion object {
                fun create(@Player.DiscontinuityReason reason: Int): Reason =
                    when (reason) {
                        Player.DISCONTINUITY_REASON_PERIOD_TRANSITION -> PeriodTransition
                        Player.DISCONTINUITY_REASON_SEEK -> Seek
                        Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> SeekAdjustment
                        Player.DISCONTINUITY_REASON_AD_INSERTION -> AdInsertion
                        Player.DISCONTINUITY_REASON_INTERNAL -> Internal
                        else -> error("Illegal DiscontinuityReason: $reason")
                    }
            }
        }
    }
    data class TimelineChanged(val timeline: List<String>, val reason: Reason): PlayerEvent() {

        enum class Reason {
            TimelineChanged, SourceUpdate;

            companion object {
                fun create(@Player.TimelineChangeReason reason: Int): Reason =
                    when (reason) {
                        Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> TimelineChanged
                        Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> SourceUpdate
                        else -> error("Illegal TimelineChangeReason: $reason")
                    }
            }
        }
    }
    data class IsPlayingChanged(val isPlaying: Boolean) : PlayerEvent()

    sealed class Error : PlayerEvent() {
        class UnsupportedUrlException(val url: String) : Error()
    }
}