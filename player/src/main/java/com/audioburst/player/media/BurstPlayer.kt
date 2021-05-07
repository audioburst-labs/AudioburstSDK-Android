package com.audioburst.player.media

import com.audioburst.library.models.Burst
import com.audioburst.library.models.Playlist
import kotlinx.coroutines.flow.StateFlow

public interface BurstPlayer {

    public val state: StateFlow<State>

    public val nowPlaying: StateFlow<NowPlaying>

    public val currentPlaylist: StateFlow<Playlist?>

    public val adState: StateFlow<AdState>

    public val currentMediaUrl: String?

    public val currentMediaDuration: Long?

    public var shouldPrefetch: Boolean

    public fun play(playlist: Playlist)

    public fun removeAt(position: Int): Burst?

    public fun playSource(burst: Burst)

    public fun play(burst: Burst)

    public fun play()

    public fun playAt(position: Int)

    public fun pause()

    public fun togglePlayback()

    public fun next()

    public fun previous()

    public fun seekTo(position: Long)

    public fun currentPlayBackPosition(): Long

    public fun setAdvertisementUrl(burst: Burst, url: String)

    public fun clear()

    public data class State(
        val isPlaying: Boolean = false,
        val isPrepared: Boolean = false,
        val isPauseEnabled: Boolean = false,
        val isSkipToNextEnabled: Boolean = false,
        val isSkipToPreviousEnabled: Boolean = false,
        val isPlayEnabled: Boolean = false,
    )

    public sealed class NowPlaying {
        public class Nothing(public val reason: Reason? = null): NowPlaying() {
            public sealed class Reason {
                public class UnsupportedUrl(
                    public val burst: Burst,
                    public val url: String,
                ): Reason()
            }
        }
        public data class Media(
            val burst: Burst,
            val mediaUrl: String,
            val positionInPlaylist: Int,
            val duration: Long,
        ): NowPlaying()
    }

    public data class AdState(
        val isAvailableInCurrentMedia: Boolean = false,
        val canSkip: Boolean = false,
        val isInAd: Boolean = false,
    )
}
