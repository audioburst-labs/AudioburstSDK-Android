package com.audioburst.player.core.models

/**
 * Describes if the currently playing media items contains an ad and whether it can be skipped already or not.
 */
public class AdState(
    public val isAvailableInCurrentMedia: Boolean = false,
    public val canSkip: Boolean = false,
) {

    internal fun copy(canSkip: Boolean): AdState =
        AdState(isAvailableInCurrentMedia = isAvailableInCurrentMedia, canSkip = canSkip)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdState

        if (isAvailableInCurrentMedia != other.isAvailableInCurrentMedia) return false
        if (canSkip != other.canSkip) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isAvailableInCurrentMedia.hashCode()
        result = 31 * result + canSkip.hashCode()
        return result
    }

    override fun toString(): String {
        return "AdState(isAvailableInCurrentMedia=$isAvailableInCurrentMedia, canSkip=$canSkip)"
    }
}