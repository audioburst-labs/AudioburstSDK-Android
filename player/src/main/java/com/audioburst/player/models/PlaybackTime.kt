package com.audioburst.player.models

import com.audioburst.library.models.Duration

/**
 * Describes what is the current playback position and what is the current media's duration.
 */
public class PlaybackTime(
    public val playbackPosition: Duration,
    public val mediaDuration: Duration,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaybackTime

        if (playbackPosition != other.playbackPosition) return false
        if (mediaDuration != other.mediaDuration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playbackPosition.hashCode()
        result = 31 * result + mediaDuration.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlaybackTime(playbackPosition=$playbackPosition, mediaDuration=$mediaDuration)"
    }
}