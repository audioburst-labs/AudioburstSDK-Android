package com.audioburst.player.core.media

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.audioburst.library.models.*
import com.audioburst.player.core.extensions.*
import com.audioburst.player.core.extensions.duration
import com.audioburst.player.core.extensions.isPauseEnabled
import com.audioburst.player.core.extensions.isPlayEnabled
import com.audioburst.player.core.extensions.isPrepared
import com.audioburst.player.core.media.events.PlayerEvent
import com.audioburst.player.core.media.events.PlayerEventFlow
import com.audioburst.player.core.media.mappers.BurstToMediaItemMapper
import com.audioburst.player.core.models.*
import com.audioburst.player.core.models.BurstIdUri
import com.audioburst.player.core.models.PlaybackState
import com.audioburst.player.core.utils.AdUrlCache
import com.audioburst.player.core.utils.CurrentPlaylistCacheSetter
import com.audioburst.player.core.utils.PlaybackTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

internal class BurstExoPlayer(
    playerEventFlow: PlayerEventFlow,
    private val scope: CoroutineScope,
    private val burstToMediaItemMapper: BurstToMediaItemMapper,
    private val mediaPlayer: MediaPlayer,
    private val adStateProvider: AdStateProvider,
    private val currentPlaylistCacheSetter: CurrentPlaylistCacheSetter,
    private val adUrlCache: AdUrlCache,
    private val playbackTimerCreator: PlaybackTimer.Creator,
) : BurstPlayer {

    private val _state = MutableStateFlow(mediaPlayer.state.value.toState(mediaPlayer.isPlaying.value))
    override val playbackState: StateFlow<PlaybackState>
        get() = _state

    private val _nowPlaying = MutableStateFlow<NowPlaying>(NowPlaying.Nothing())
    override val nowPlaying: StateFlow<NowPlaying>
        get() = _nowPlaying

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    override val currentPlaylist: StateFlow<Playlist?>
        get() = _currentPlaylist

    override val adState: StateFlow<AdState?>
        get() = adStateProvider.adState

    override val currentMediaUrl: String?
        get() = (_nowPlaying.value as? NowPlaying.Media)?.mediaUrl?.url

    override val currentMediaDuration: Duration?
        get() = (_nowPlaying.value as? NowPlaying.Media)?.duration

    override val currentPlaybackPosition: Duration
        get() = mediaPlayer.playbackPosition.toDouble().toDuration(DurationUnit.Milliseconds)

    private var shouldPrefetch: Boolean = false

    init {
        mediaPlayer.state
            .combine(mediaPlayer.isPlaying) { state, isPlaying ->
                _state.value = state.toState(isPlaying)
            }
            .launchIn(scope)

        mediaPlayer.nowPlaying
            .onEach { _nowPlaying.value = it.toNowPlaying(currentPlaylist.value?.bursts ?: emptyList(), adUrlCache) }
            .launchIn(scope)

        _nowPlaying
            .onEach { adStateProvider.onNowPlaying(it) }
            .launchIn(scope)

        observeError(playerEventFlow)
    }

    private fun observeError(playerEventFlow: PlayerEventFlow) {
        playerEventFlow()
            .filterIsInstance<PlayerEvent.Error.UnsupportedUrlException>()
            .onEach {
                val url = it.url
                val nowPlaying = _nowPlaying.value
                if (nowPlaying is NowPlaying.Media && nowPlaying.burst.contains(url)) {
                    _nowPlaying.value = NowPlaying.Nothing(
                        NowPlaying.Nothing.Reason.UnsupportedUrl(
                            burst = nowPlaying.burst,
                            url = url,
                        )
                    )
                }
            }
            .launchIn(scope)
    }

    private fun Burst.contains(url: String): Boolean =
        url == audioUrl || url == streamUrl || url == source.audioUrl

    override fun load(playlist: Playlist, playWhenReady: Boolean) {
        if (playlist.bursts.isNotEmpty()) {
            prepareAndPlay(
                playlist = playlist,
                playWhenReady = playWhenReady,
            )
            onNewPlaylist(playlist)
        }
    }

    private fun onNewPlaylist(playlist: Playlist) {
        _currentPlaylist.value = playlist
        currentPlaylistCacheSetter.setCurrentPlaylist(playlist)
        if (currentPlaylist.value?.id != playlist.id) {
            adUrlCache.clear()
        }
    }

    private fun prepareAndPlay(playlist: Playlist, idToPlay: String? = null, playWhenReady: Boolean = true) {
        val mediaItems = playlist.bursts.map(burstToMediaItemMapper::map)
        if (currentPlaylist.value?.id == playlist.id) {
            mediaPlayer.addAll(
                list = mediaItems,
                shouldUseCache = shouldPrefetch,
            )
            if (playWhenReady) {
                play()
            }
        } else {
            mediaPlayer.play(
                list = mediaItems,
                shouldUseCache = shouldPrefetch,
                playWhenReady = playWhenReady,
                mediaId = idToPlay ?: mediaItems.first().mediaId,
            )
        }
    }

    override fun switchToBurstSource(burst: Burst): Boolean {
        val mediaItem = burstToMediaItemMapper.mapWithSource(burst) ?: return false
        val seekTo = mediaPlayer.playbackPosition + burst.source.durationFromStart.milliseconds.toLong()
        val position = mediaPlayer.currentTimeline.indexOf(burst.id)
        return if (position != -1) {
            mediaPlayer.replaceAt(mediaItem, position, seekTo)
            true
        } else {
            false
        }
    }

    override fun switchToBurst(burst: Burst): Boolean {
        val position = mediaPlayer.currentTimeline.indexOf(burst.id)
        val mediaItem = burstToMediaItemMapper.map(burst)
        return if (position != -1) {
            mediaPlayer.replaceAt(mediaItem, position, 0L)
            true
        } else {
            false
        }
    }

    override fun play() {
        mediaPlayer.play()
        mediaPlayer.prepareOnIdle()
    }

    override fun playAt(position: Int): Boolean =
        if (mediaPlayer.currentTimeline.elementAtOrNull(position) != null) {
            mediaPlayer.playAt(position)
            mediaPlayer.prepareOnIdle()
            true
        } else {
            false
        }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun togglePlayback() {
        val state = playbackState.value
        when {
            state.isPlaying -> pause()
            state.isPlayEnabled -> play()
        }
    }

    override fun next(): Boolean =
        if (playbackState.value.isSkipToNextEnabled) {
            mediaPlayer.skipToNext()
            mediaPlayer.prepareOnIdle()
            true
        } else {
            false
        }

    override fun previous(): Boolean =
        if (playbackState.value.isSkipToPreviousEnabled) {
            mediaPlayer.skipToPrevious()
            mediaPlayer.prepareOnIdle()
            true
        } else {
            false
        }

    override fun seekTo(position: Duration) {
        mediaPlayer.seekTo(position.milliseconds.toLong())
    }

    override fun playbackTime(updateInterval: Duration): StateFlow<PlaybackTime> =
        playbackTimerCreator.create(updateInterval).playbackTime

    internal fun clear() {
        mediaPlayer.stopPlayback()
        adStateProvider.finish()
    }
}

private fun MediaMetadataCompat.toNowPlaying(currentBursts: List<Burst>, adUrlCache: AdUrlCache): NowPlaying {
    if (this == NOTHING_PLAYING) return NowPlaying.Nothing()
    val id = description.mediaId ?: return NowPlaying.Nothing()
    val duration = duration
    val uri = description.mediaUri ?: return NowPlaying.Nothing()
    val burst = currentBursts.firstOrNull { it.id == id } ?: return NowPlaying.Nothing()
    val mediaUrl = uri.toMediaUrl(burst, adUrlCache.get(burst)) ?: return NowPlaying.Nothing()
    return NowPlaying.Media(
        burst = burst,
        duration = duration.toDouble().toDuration(DurationUnit.Milliseconds),
        positionInPlaylist = currentBursts.indexOf(burst),
        mediaUrl = mediaUrl,
    )
}

private fun Uri.toMediaUrl(burst: Burst, adUrl: String?): MediaUrl? =
    when (val stringUrl = toString()) {
        burst.audioUrl, burst.streamUrl -> MediaUrl.Burst(stringUrl)
        BurstIdUri(burst).uri.toString() -> if (adUrl != null) {
            MediaUrl.Advertisement(adUrl)
        } else {
            MediaUrl.Burst(burst.audioUrl)
        }
        burst.source.audioUrl -> MediaUrl.Source(stringUrl)
        else -> null
    }

private fun PlaybackStateCompat.toState(isPlaying: Boolean): PlaybackState =
    PlaybackState(
        isPlaying = isPlaying,
        isPrepared = isPrepared,
        isPauseEnabled = isPauseEnabled,
        isSkipToNextEnabled = isSkipToNextEnabled,
        isSkipToPreviousEnabled = isSkipToPreviousEnabled,
        isPlayEnabled = isPlayEnabled
    )