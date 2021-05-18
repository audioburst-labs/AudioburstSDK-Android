package com.audioburst.player.models

import com.audioburst.library.models.Burst
import com.audioburst.library.models.Duration

/**
 * Class that describes whether there is something currently playing or not.
 */
public sealed class NowPlaying {

    /**
     * Indicates that there is nothing currently playing.
     */
    public class Nothing(public val reason: Reason? = null): NowPlaying() {
        public sealed class Reason {
            public class UnsupportedUrl(
                public val burst: Burst,
                public val url: String,
            ): Reason()
        }
    }

    /**
     * Describes what media item is currently playing.
     */
    public class Media(
        public val burst: Burst,
        public val mediaUrl: MediaUrl,
        public val positionInPlaylist: Int,
        public val duration: Duration,
    ): NowPlaying() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Media

            if (burst != other.burst) return false
            if (mediaUrl != other.mediaUrl) return false
            if (positionInPlaylist != other.positionInPlaylist) return false
            if (duration != other.duration) return false

            return true
        }

        override fun hashCode(): Int {
            var result = burst.hashCode()
            result = 31 * result + mediaUrl.hashCode()
            result = 31 * result + positionInPlaylist
            result = 31 * result + duration.hashCode()
            return result
        }

        override fun toString(): String {
            return "Media(burst=$burst, mediaUrl=$mediaUrl, positionInPlaylist=$positionInPlaylist, duration=$duration)"
        }
    }
}