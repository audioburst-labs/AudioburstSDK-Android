package com.audioburst.player.media

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.audioburst.library.models.Burst
import com.audioburst.library.models.Playlist
import com.audioburst.player.extensions.*
import com.audioburst.player.extensions.isPrepared
import com.audioburst.player.media.events.PlayerEvent
import com.audioburst.player.media.events.PlayerEventFlow
import com.audioburst.player.media.mappers.BurstToMediaItemMapper
import com.audioburst.player.models.BurstIdUri
import com.audioburst.player.models.MediaUrl
import com.audioburst.player.utils.AdUrlCache
import com.audioburst.player.utils.CurrentPlaylistCacheSetter
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class BurstExoPlayer(
    playerEventFlow: PlayerEventFlow,
    private val scope: CoroutineScope,
    private val burstToMediaItemMapper: BurstToMediaItemMapper,
    private val mediaPlayer: MediaPlayer,
    private val adStateProvider: AdStateProvider,
    private val currentPlaylistCacheSetter: CurrentPlaylistCacheSetter,
    private val adUrlCache: AdUrlCache,
) : BurstPlayer {

    private val _state = MutableStateFlow(mediaPlayer.state.value.toState(mediaPlayer.isPlaying.value))
    override val state: StateFlow<BurstPlayer.State>
        get() = _state

    private val _nowPlaying = MutableStateFlow<BurstPlayer.NowPlaying>(BurstPlayer.NowPlaying.Nothing())
    override val nowPlaying: StateFlow<BurstPlayer.NowPlaying>
        get() = _nowPlaying

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    override val currentPlaylist: StateFlow<Playlist?>
        get() = _currentPlaylist

    override val adState: StateFlow<BurstPlayer.AdState?>
        get() = adStateProvider.adState

    override val currentMediaUrl: String?
        get() = (_nowPlaying.value as? BurstPlayer.NowPlaying.Media)?.mediaUrl?.url

    override val currentMediaDuration: Long?
        get() = (_nowPlaying.value as? BurstPlayer.NowPlaying.Media)?.duration

    override var shouldPrefetch: Boolean = false

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
                if (nowPlaying is BurstPlayer.NowPlaying.Media && nowPlaying.burst.contains(url)) {
                    _nowPlaying.value = BurstPlayer.NowPlaying.Nothing(
                        BurstPlayer.NowPlaying.Nothing.Reason.UnsupportedUrl(
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

    override fun play(playlist: Playlist) {
        prepareAndPlay(bursts = playlist.bursts)
        onNewPlaylist(playlist)
    }

    private fun onNewPlaylist(playlist: Playlist) {
        _currentPlaylist.value = playlist
        currentPlaylistCacheSetter.setCurrentPlaylist(playlist)
        adUrlCache.clear()
    }

    private fun prepareAndPlay(bursts: List<Burst>, idToPlay: String? = null, playWhenReady: Boolean = true) {
        scope.launch {
            if (bursts.isEmpty()) return@launch
            pause()
            prepareAndPlay(
                id = idToPlay ?: bursts.first().id,
                playWhenReady = playWhenReady,
                mediaItems = bursts.map(burstToMediaItemMapper::map),
            )
        }
    }

    private fun prepareAndPlay(id: String, mediaItems: List<MediaItem>, playWhenReady: Boolean) {
        val nowPlayingId = (nowPlaying.value as? BurstPlayer.NowPlaying.Media)?.burst?.id
        if (state.value.isPrepared && id == nowPlayingId) {
            if (playWhenReady) {
                play()
            }
        } else {
            mediaPlayer.play(
                list = mediaItems,
                shouldUseCache = shouldPrefetch,
                playWhenReady = playWhenReady,
                mediaId = id
            )
            if (playWhenReady) {
                play()
            }
        }
    }

    override fun removeAt(position: Int): Burst? {
        val burst = currentPlaylist.value?.bursts?.getOrNull(position)
        val isSuccess = mediaPlayer.removeAt(position)
        return if (isSuccess && burst != null) {
            burst
        } else {
            null
        }
    }

    override fun playSource(burst: Burst) {
        scope.launch {
            val mediaItem = burstToMediaItemMapper.mapWithSource(burst) ?: return@launch
            val seekTo = mediaPlayer.playbackPosition + burst.source.durationFromStart.milliseconds.toLong()
            val position = mediaPlayer.currentTimeline.indexOf(burst.id)
            mediaPlayer.replaceAt(mediaItem, position, seekTo)
        }
    }

    override fun play(burst: Burst) {
        scope.launch {
            val position = mediaPlayer.currentTimeline.indexOf(burst.id)
            val mediaItem = burstToMediaItemMapper.map(burst)
            mediaPlayer.replaceAt(mediaItem, position, 0L)
        }
    }

    override fun play() {
        mediaPlayer.play()
        mediaPlayer.prepareOnIdle()
    }

    override fun playAt(position: Int) {
        mediaPlayer.playAt(position)
        mediaPlayer.prepareOnIdle()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun togglePlayback() {
        val state = state.value
        when {
            state.isPlaying -> pause()
            state.isPlayEnabled -> play()
        }
    }

    override fun next() {
        if (state.value.isSkipToNextEnabled) {
            mediaPlayer.skipToNext()
            mediaPlayer.prepareOnIdle()
        }
    }

    override fun previous() {
        if (state.value.isSkipToPreviousEnabled) {
            mediaPlayer.skipToPrevious()
        } else {
            seekTo(0)
        }
        mediaPlayer.prepareOnIdle()
    }

    override fun seekTo(position: Long) {
        mediaPlayer.seekTo(position)
    }

    override fun currentPlayBackPosition(): Long = mediaPlayer.playbackPosition

    override fun clear() {
        mediaPlayer.stopPlayback()
        adStateProvider.finish()
    }
}

private inline val MediaMetadataCompat.duration: Long
    get() = this.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

private fun MediaMetadataCompat.toNowPlaying(currentBursts: List<Burst>, adUrlCache: AdUrlCache): BurstPlayer.NowPlaying {
    if (this == NOTHING_PLAYING) return BurstPlayer.NowPlaying.Nothing()
    val id = description.mediaId ?: return BurstPlayer.NowPlaying.Nothing()
    val duration = duration
    val uri = description.mediaUri ?: return BurstPlayer.NowPlaying.Nothing()
    val burst = currentBursts.firstOrNull { it.id == id } ?: return BurstPlayer.NowPlaying.Nothing()
    val mediaUrl = uri.toMediaUrl(burst, adUrlCache.get(burst)) ?: return BurstPlayer.NowPlaying.Nothing()
    return BurstPlayer.NowPlaying.Media(
        burst = burst,
        duration = duration,
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

private fun PlaybackStateCompat.toState(isPlaying: Boolean): BurstPlayer.State =
    BurstPlayer.State(
        isPlaying = isPlaying,
        isPrepared = isPrepared,
        isPauseEnabled = isPauseEnabled,
        isSkipToNextEnabled = isSkipToNextEnabled,
        isSkipToPreviousEnabled = isSkipToPreviousEnabled,
        isPlayEnabled = isPlayEnabled
    )