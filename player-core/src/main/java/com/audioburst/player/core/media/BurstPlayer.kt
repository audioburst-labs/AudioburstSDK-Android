package com.audioburst.player.core.media

import com.audioburst.library.models.Burst
import com.audioburst.library.models.Duration
import com.audioburst.library.models.Playlist
import com.audioburst.player.core.models.AdState
import com.audioburst.player.core.models.NowPlaying
import com.audioburst.player.core.models.PlaybackState
import com.audioburst.player.core.models.PlaybackTime
import kotlinx.coroutines.flow.StateFlow

/**
 * Media player that plays [Playlist] (list of [Burst]s). Instances can be obtained from [AudioburstPlayerCore].
 *
 * You should use this class to control media playback and observe its current state.
 */
public interface BurstPlayer {

    /**
     * [StateFlow] of current [PlaybackState].
     */
    public val playbackState: StateFlow<PlaybackState>

    /**
     * [StateFlow] that describes what is currently [NowPlaying].
     */
    public val nowPlaying: StateFlow<NowPlaying>

    /**
     * [StateFlow] that describes what is currently [Playlist].
     */
    public val currentPlaylist: StateFlow<Playlist?>

    /**
     * [StateFlow] of current [AdState].
     */
    public val adState: StateFlow<AdState?>

    /**
     * Returns what URL is currently being played.
     */
    public val currentMediaUrl: String?

    /**
     * Returns what is the [Duration] of current media item.
     */
    public val currentMediaDuration: Duration?

    /**
     * Returns what is the current playback position described in [Duration].
     */
    public val currentPlaybackPosition: Duration

    /**
     * Loads a [Playlist].
     *
     * @param playlist The [Playlist] to load.
     * @param playWhenReady Whether playback should proceed when ready.
     */
    public fun load(playlist: Playlist, playWhenReady: Boolean)

    /**
     * Trying to start playback if there is any [Playlist] ready.
     */
    public fun play()

    /**
     * Pauses playback.
     */
    public fun pause()

    /**
     * Trying to move to the next [Burst].
     *
     * @return Whether it was possible to move to next.
     */
    public fun next(): Boolean

    /**
     * Trying to move to the previous [Burst].
     *
     * @return Whether it was possible to move to previous.
     */
    public fun previous(): Boolean

    /**
     * Replaces a media item on the playlist with the same [Burst]s id with the provided [Burst].
     * This function lets you create a functionality to switch back from playing [BurstSource] to [Burst].
     *
     * @param burst The [Burst] whose content player should play.
     * @return Whether it was possible to switch to a provided [Burst].
     */
    public fun switchToBurst(burst: Burst): Boolean

    /**
     * Replaces a media item on the playlist with the same [Burst]s id with the provided [Burst]s [BurstSource].
     * This function lets you create a functionality to switch back from playing [Burst] to [BurstSource].
     *
     * @param burst The [Burst] whose [BurstSource] content player should play.
     * @return Whether [Burst] has [BurstSource] and it was possible to switch to a provided [Burst].
     */
    public fun switchToBurstSource(burst: Burst): Boolean

    /**
     * Trying to move to the provided position.
     *
     * @param position The position to which player should move.
     * @return Whether it there was a [Burst] at the provided [position]
     */
    public fun playAt(position: Int): Boolean

    /**
     * If player is currently playing it will pause it. Otherwise it will try to start playback.
     */
    public fun togglePlayback()

    /**
     * Trying to seek to the provided position.
     *
     * @param position The time to which player should seek.
     */
    public fun seekTo(position: Duration)

    /**
     * Returns a [StateFlow] of [PlaybackTime]. It emits new state after each [updateInterval] time
     * pass. It emits that only when the playback happens.
     *
     * @param updateInterval Interval of the [PlaybackTime] update.
     */
    public fun playbackTime(updateInterval: Duration): StateFlow<PlaybackTime>
}
