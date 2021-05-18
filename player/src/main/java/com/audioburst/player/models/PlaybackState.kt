package com.audioburst.player.models

/**
 * Class that describes what is the current playback state. [BurstPlayer] emits a new instance of this
 * class everytime playback state changes.
 */
public class PlaybackState(
    public val isPlaying: Boolean = false,
    public val isPrepared: Boolean = false,
    public val isPauseEnabled: Boolean = false,
    public val isSkipToNextEnabled: Boolean = false,
    public val isSkipToPreviousEnabled: Boolean = false,
    public val isPlayEnabled: Boolean = false,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaybackState

        if (isPlaying != other.isPlaying) return false
        if (isPrepared != other.isPrepared) return false
        if (isPauseEnabled != other.isPauseEnabled) return false
        if (isSkipToNextEnabled != other.isSkipToNextEnabled) return false
        if (isSkipToPreviousEnabled != other.isSkipToPreviousEnabled) return false
        if (isPlayEnabled != other.isPlayEnabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isPlaying.hashCode()
        result = 31 * result + isPrepared.hashCode()
        result = 31 * result + isPauseEnabled.hashCode()
        result = 31 * result + isSkipToNextEnabled.hashCode()
        result = 31 * result + isSkipToPreviousEnabled.hashCode()
        result = 31 * result + isPlayEnabled.hashCode()
        return result
    }

    override fun toString(): String {
        return "State(isPlaying=$isPlaying, isPrepared=$isPrepared, isPauseEnabled=$isPauseEnabled, isSkipToNextEnabled=$isSkipToNextEnabled, isSkipToPreviousEnabled=$isSkipToPreviousEnabled, isPlayEnabled=$isPlayEnabled)"
    }
}