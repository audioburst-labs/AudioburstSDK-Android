package com.audioburst.player.controller

import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.Burst
import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.controller.extensions.airedText
import com.audioburst.player.controller.extensions.verticalGradient
import com.audioburst.player.controller.models.*
import com.audioburst.player.controller.utils.*
import com.audioburst.player.controller.utils.DateUtils
import com.audioburst.player.controller.utils.ListenedBurstIds
import com.audioburst.player.controller.utils.PlayerStyleHolder
import com.audioburst.player.controller.utils.UrlOpener
import com.audioburst.player.core.media.BurstPlayer
import com.audioburst.player.core.models.MediaUrl
import com.audioburst.player.core.models.NowPlaying
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.audioburst.library.models.UiEvent as LibrarysUiEvent

internal class BurstPlayerController(
    private val burstPlayer: BurstPlayer,
    private val scope: CoroutineScope,
    private val resourceProvider: ResourceProvider,
    private val listenedBurstIds: ListenedBurstIds,
    private val playerStyleHolder: PlayerStyleHolder,
    private val urlOpener: UrlOpener,
    private val audioburstLibrary: AudioburstLibrary,
) : PlayerController {

    private val _playbackItemPosition = MutableStateFlow(0)
    override val playbackItemPosition = _playbackItemPosition.asStateFlow()
    
    private val _scrollListToTop = Channel<Unit>()
    override val scrollListToTop = _scrollListToTop.receiveAsFlow()
    
    private val _cardItems = MutableStateFlow<List<PlaylistCardItem>>(emptyList())
    override val cardItems = _cardItems.asStateFlow()
    
    private val _listItems = MutableStateFlow<List<PlaylistItem>>(emptyList())
    override val listItems = _listItems.asStateFlow()
    
    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState = _playerState.asStateFlow()
    
    private val _playerProgressBar = MutableStateFlow(PlayerProgressBar(callback = ::onEvent))
    override val playerProgressBar = _playerProgressBar.asStateFlow()
    
    private val _isSwipeTutorialVisible = Channel<Boolean>()
    override val isSwipeTutorialVisible = _isSwipeTutorialVisible.receiveAsFlow()
    
    private val _collectionState = MutableStateFlow(PlayerCollectionState.Cards)
    override val collectionState = _collectionState.asStateFlow()
    
    private val _adOverlay = MutableStateFlow(AdOverlayState())
    override val adOverlay = _adOverlay.asStateFlow()

    init {
        observeCurrentPlaylist()
        observeNowPlaying()
        observeAdState()
        observeState()
    }

    private fun observeCurrentPlaylist() {
        burstPlayer.currentPlaylist
            .filterNotNull()
            .onEach {
                _listItems.value = listItems(it.bursts, (burstPlayer.nowPlaying.value as? NowPlaying.Media)?.burst?.id)
                _cardItems.value = items(it.bursts)
            }
            .launchIn(scope)
    }

    private fun observeNowPlaying() {
        burstPlayer.nowPlaying
            .filterIsInstance<NowPlaying.Media>()
            .onEach { media ->
                val playlist = burstPlayer.currentPlaylist.value ?: return@onEach
                val index = playlist.bursts.indexOfFirst { it.id == media.burst.id }
                if (index != -1) {
                    _cardItems.value = items(playlist.bursts)
                }

                listenedBurstIds.add(media.burst.id)
                _listItems.value = listItems(playlist.bursts, media.burst.id)

//                _playerState.value = _playerState.value.copy(
//                    isListShown = collectionState.value == PlayerCollectionState.List
//                )

                _playerProgressBar.value = _playerProgressBar.value.copy(
                    totalTime = DateUtils.formatAsTime(media.duration.milliseconds.toLong(), resourceProvider),
                    maxProgress = media.duration.milliseconds.toInt(),
                )

                _playerState.value = _playerState.value.copy(
                    title = media.burst.showName,
                    subtitle = media.burst.title,
//                    maxProgress = media.duration.toInt(),
//                    actionButton = media.burst.ctaData?.let { ctaData ->
//                        MiniPlayerView.ActionButton(ctaData.buttonText) {
//                            onCtaButtonClick(ctaData)
//                        }
//                    }
                )
            }
            .launchIn(scope)

        burstPlayer.nowPlaying
            .filterIsInstance<NowPlaying.Nothing>()
            .mapNotNull { it.reason }
            .filterIsInstance<NowPlaying.Nothing.Reason.UnsupportedUrl>()
            .filter { it.burst.source.audioUrl == it.url }
            .onEach {
                burstPlayer.switchToBurst(it.burst)
                burstPlayer.play()
            }
            .launchIn(scope)
    }

    private fun observeAdState() {
        burstPlayer.adState
            .onEach { adState ->
                _adOverlay.value = _adOverlay.value.copy(
                    adTimeLeft = adTimeLeft() ?: 0,
                    isVisible = adState != null,
                    isSkipButtonVisible = adState?.canSkip == true,
                )

                _playerProgressBar.value = _playerProgressBar.value.copy(
                    isSeekable = adState?.isAvailableInCurrentMedia == false || (adState?.isAvailableInCurrentMedia == true && adState.canSkip)
                )
            }
            .launchIn(scope)
    }

    private fun observeState() {
        burstPlayer.playbackState
            .onEach { playbackState ->
                _playerState.value = _playerState.value.copy(
                    isPlaying = playbackState.isPlaying,
                    isNextEnabled = playbackState.isSkipToNextEnabled,
                    isPreviousEnabled = playbackState.isSkipToPreviousEnabled
                )

                _playerProgressBar.value = _playerProgressBar.value.copy(isPlaying = playbackState.isPlaying)
            }
            .launchIn(scope)
    }

    private fun items(bursts: List<Burst>): List<PlaylistCardItem> =
        bursts.map { burst ->
            PlaylistCardItem(
                id = burst.id,
                bottomSubtitle = burst.sourceName,
                bottomTitle = burst.showName,
                timeText = burst.airedText(resourceProvider),
                title = burst.title,
                isLoadingFullShow = false,
                colorAccent = playerStyleHolder.playerStyle.value.colorAccent,
                gradient = burst.verticalGradient,
                callback = ::onEvent,
            )
        }

    private fun adTimeLeft(): Int? {
        val adDurationMs = currentAdEndPositionMs() ?: return null
        val currentPlaybackPosition = burstPlayer.currentPlaybackPosition.milliseconds.toLong()
        return if (currentPlaybackPosition <= adDurationMs) {
            (adDurationMs - currentPlaybackPosition).toInt()
        } else {
            null
        }
    }

    private fun listItems(bursts: List<Burst>, nowPlayingId: String?): List<PlaylistItem> =
        bursts.map { burst ->
            PlaylistItem(
                id = burst.id,
                title = burst.title,
                subtitle = burst.showName,
                rightText = rightText(burst, nowPlayingId),
                isPlaying = burst.id == nowPlayingId,
                theme = playerStyleHolder.playerStyle.value.playerTheme,
                callback = ::onEvent
            )
        }

    private fun rightText(burst: Burst, nowPlayingId: String?): PlaylistItem.RightText =
        when {
            nowPlayingId != null && nowPlayingId == burst.id -> PlaylistItem.RightText.Playing
            listenedBurstIds.contains(burst.id) -> PlaylistItem.RightText.Listened
            else -> PlaylistItem.RightText.Time
        }

    private fun currentlyPlayingMedia(): NowPlaying.Media? = burstPlayer.nowPlaying.value as? NowPlaying.Media

    private fun currentBurst(): Burst? = currentlyPlayingMedia()?.burst

    private fun currentAdEndPositionMs(): Long? = burstPlayer.adState.value?.isAvailableInCurrentMedia?.let {
        burstPlayer.currentMediaDuration?.milliseconds?.toLong()
    }
    
    override fun onEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            UiEvent.NextButtonClick -> burstPlayer.next()
            is UiEvent.OnCtaButtonClick -> uiEvent.handle()
            is UiEvent.OnLikeButtonClick -> uiEvent.handle()
            is UiEvent.OnPlaylistItemClick -> uiEvent.handle()
            UiEvent.OnRewindButtonClick -> burstPlayer.seekTo(Duration.ZERO)
            is UiEvent.OnSeekTo -> uiEvent.handle()
            is UiEvent.OnShareButtonClick -> uiEvent.handle()
            UiEvent.OnStartSeeking -> burstPlayer.pause()
            is UiEvent.OnSwitchSource -> switchSource()
            UiEvent.PlayPauseButtonClick -> burstPlayer.togglePlayback()
            UiEvent.PreviousButtonClick -> burstPlayer.previous()
            UiEvent.SkipAdButtonClick -> burstPlayer.next()
            UiEvent.OnShareOptionsClose -> _playerState.value = _playerState.value.copy(shareOptions = null)
            is UiEvent.OnShareOptionsChosen -> uiEvent.handle()
        }
    }

    private fun UiEvent.OnCtaButtonClick.handle() {
        val burst = burstIndicator.burst ?: return
        val ctaData = burst.ctaData ?: return
        if (urlOpener.open(ctaData.url)) {
            audioburstLibrary.ctaButtonClick(burstId = burst.id)
        }
    }

    private fun UiEvent.OnLikeButtonClick.handle() {
        val burst = burstIndicator.burst ?: return
        audioburstLibrary.report(
            uiEvent = LibrarysUiEvent.ThumbsUp,
            burstId = burst.id,
            isPlaying = burstPlayer.isPlaying,
        )
    }

    private fun UiEvent.OnPlaylistItemClick.handle() {
        val burst = burstIndicator.burst ?: return
        val isPlaylistShown = _playerState.value.playlistButtonState == PlayerState.PlaylistButtonState.List
        _playerState.value = _playerState.value.copy(playlistButtonState = if (isPlaylistShown) PlayerState.PlaylistButtonState.Cards else PlayerState.PlaylistButtonState.List)
        audioburstLibrary.report(
            uiEvent = if (isPlaylistShown) LibrarysUiEvent.PlaylistClose else LibrarysUiEvent.PlaylistClick,
            burstId = burst.id,
            isPlaying = burstPlayer.isPlaying,
        )
    }

    private fun UiEvent.OnSeekTo.handle() {
        burstPlayer.seekTo(position.toDouble().toDuration(DurationUnit.Milliseconds))
    }

    private fun UiEvent.OnShareButtonClick.handle() {
        scope.launch {
            val burst = burstIndicator.burst ?: return@launch
            _playerState.value = _playerState.value.copy(isLoadingShareUrl = true)
            val options = audioburstLibrary.getShareOptions(burstId = burst.id)
            _playerState.value = _playerState.value.copy(
                isLoadingShareUrl = false,
                shareOptions = options,
            )
            audioburstLibrary.report(
                uiEvent = LibrarysUiEvent.ShareBurst,
                burstId = burst.id,
                isPlaying = burstPlayer.isPlaying,
            )
        }
    }

    private fun switchSource() {
        val nowPlayingMedia = currentlyPlayingMedia() ?: return
        val nowPlayingUrl = nowPlayingMedia.mediaUrl
        val burst = nowPlayingMedia.burst

        when (nowPlayingUrl) {
            is MediaUrl.Advertisement, is MediaUrl.Burst -> burstPlayer.switchToBurstSource(burst)
            is MediaUrl.Source -> burstPlayer.switchToBurst(burst)
        }
    }

    private fun UiEvent.OnShareOptionsChosen.handle() {
        urlOpener.share(
            text = shareData.title,
            subject = shareData.message,
        )
    }

    private val BurstPlayer.isPlaying: Boolean
        get() = playbackState.value.isPlaying

    private val UiEvent.BurstIndicator.burst: Burst?
        get() = when (this) {
            is UiEvent.BurstIndicator.BurstId -> burstPlayer.currentPlaylist.value?.bursts?.firstOrNull { it.id == value }
            is UiEvent.BurstIndicator.Position -> burstPlayer.currentPlaylist.value?.bursts?.getOrNull(value)
        }
}