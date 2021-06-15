package com.audioburst.player.core.media

import com.audioburst.library.models.*
import com.audioburst.player.core.models.*
import com.audioburst.player.core.models.PlaybackState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Implementation of the [BurstPlayer] that issues commands to the current [BurstExoPlayer] instance.
 * Concrete instantiation of the [BurstPlayer] is always done when [MediaService] instance is created.
 * It usually takes few milliseconds to create a service, which means we would need to return nullable
 * [BurstPlayer] from the [AudioburstPlayerCore] and provide a mechanism to listen to the "connected" event.
 * This implementation lets us avoid this what makes it easier to work with the library on the consumer side.
 *
 * This class also makes sure we are passing commands only when library is connected to the [MediaService].
 */
internal class BurstPlayerDelegate(
    mainDispatcher: CoroutineDispatcher,
    private val mediaSessionConnection: MediaSessionConnection,
) : BurstPlayer {

    private val scope = CoroutineScope(context = mainDispatcher + SupervisorJob())
    private val isConnected: Boolean
        get() = mediaSessionConnection.isConnected

    private var burstPlayer: BurstPlayer? = null
    private val intervalWithPlaybackTime: MutableList<Pair<Duration, MutableStateFlow<PlaybackTime>>> = mutableListOf()

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState>
        get() = _playbackState

    private val _nowPlaying = MutableStateFlow<NowPlaying>(NowPlaying.Nothing())
    override val nowPlaying: StateFlow<NowPlaying>
        get() = _nowPlaying

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    override val currentPlaylist: StateFlow<Playlist?>
        get() = _currentPlaylist

    private val _adState = MutableStateFlow<AdState?>(null)
    override val adState: StateFlow<AdState?>
        get() = _adState

    override val currentMediaUrl: String?
        get() = burstPlayer?.currentMediaUrl

    override val currentMediaDuration: Duration?
        get() = burstPlayer?.currentMediaDuration

    override val currentPlaybackPosition: Duration
        get() = burstPlayer?.currentPlaybackPosition ?: 0.0.toDuration(DurationUnit.Milliseconds)

    internal fun setBurstPlayer(burstPlayer: BurstPlayer) {
        scope.coroutineContext.cancelChildren()
        this.burstPlayer = burstPlayer
        burstPlayer
            .playbackState
            .onEach { _playbackState.value = it }
            .launchIn(scope)
        burstPlayer
            .nowPlaying
            .onEach { _nowPlaying.value = it }
            .launchIn(scope)
        burstPlayer
            .currentPlaylist
            .onEach { _currentPlaylist.value = it }
            .launchIn(scope)
        burstPlayer
            .adState
            .onEach { _adState.value = it }
            .launchIn(scope)
        intervalWithPlaybackTime.forEach {
            val (duration, mutableStateFlow) = it
            burstPlayer
                .playbackTime(duration)
                .onEach { playbackTime -> mutableStateFlow.value = playbackTime }
                .launchIn(scope)
        }
    }

    private fun issueBooleanCommand(command: BurstPlayer.() -> Boolean): Boolean {
        val player = burstPlayer
        return if (isConnected && player != null) {
            command(player)
        } else {
            false
        }
    }

    private fun issueCommand(command: BurstPlayer.() -> Unit) {
        val player = burstPlayer
        if (isConnected && player != null) {
            command(player)
        }
    }

    override fun load(playlist: Playlist, playWhenReady: Boolean) = issueCommand {
        load(playlist, playWhenReady)
    }

    override fun play() = issueCommand {
        play()
    }

    override fun pause() = issueCommand {
        pause()
    }

    override fun next(): Boolean = issueBooleanCommand {
        next()
    }

    override fun previous(): Boolean = issueBooleanCommand {
        previous()
    }

    override fun switchToBurst(burst: Burst): Boolean = issueBooleanCommand {
        switchToBurst(burst)
    }

    override fun switchToBurstSource(burst: Burst): Boolean = issueBooleanCommand {
        switchToBurstSource(burst)
    }

    override fun playAt(position: Int): Boolean = issueBooleanCommand {
        playAt(position)
    }

    override fun togglePlayback() = issueCommand {
        togglePlayback()
    }

    override fun seekTo(position: Duration) = issueCommand {
        seekTo(position)
    }

    override fun playbackTime(updateInterval: Duration): StateFlow<PlaybackTime> {
        val player = burstPlayer
        return if (player != null && isConnected) {
            player.playbackTime(updateInterval)
        } else {
            val mutableStateFlow = MutableStateFlow(
                PlaybackTime(
                    playbackPosition = 0.0.toDuration(DurationUnit.Milliseconds),
                    mediaDuration = 0.0.toDuration(DurationUnit.Milliseconds),
                )
            )
            intervalWithPlaybackTime.add(updateInterval to mutableStateFlow)
            mutableStateFlow
        }
    }
}