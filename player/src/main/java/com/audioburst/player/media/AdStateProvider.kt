package com.audioburst.player.media

import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.models.MediaUrl
import com.audioburst.player.utils.ListenedMediaObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface AdStateProvider {

    val adState: StateFlow<BurstPlayer.AdState?>

    fun onNowPlaying(nowPlaying: BurstPlayer.NowPlaying)

    fun finish()
}

internal class TimeAwareAdStateProvider(
    private val listenedMediaObserver: ListenedMediaObserver,
) : AdStateProvider {

    private var currentMedia: BurstPlayer.NowPlaying.Media? = null

    private val _adState = MutableStateFlow<BurstPlayer.AdState?>(null)
    override val adState: StateFlow<BurstPlayer.AdState?>
        get() = _adState.asStateFlow()

    init {
        listenedMediaObserver.onListenedObserver = {
            if (_adState.value?.isAvailableInCurrentMedia == true) {
                _adState.value = _adState.value?.copy(canSkip = true)
            }
        }
    }

    override fun onNowPlaying(nowPlaying: BurstPlayer.NowPlaying) {
        when (nowPlaying) {
            is BurstPlayer.NowPlaying.Media -> onMedia(nowPlaying)
            is BurstPlayer.NowPlaying.Nothing -> {
                _adState.value = null
                currentMedia = null
            }
        }
    }

    private fun onMedia(media: BurstPlayer.NowPlaying.Media) {
        if (currentMedia?.burst == media.burst && currentMedia?.mediaUrl == media.mediaUrl) {
            return
        }
        currentMedia = media
        val minimumListenedTimeForMedia = if (media.mediaUrl is MediaUrl.Advertisement) minimumAdListenTime else null
        _adState.value = BurstPlayer.AdState(isAvailableInCurrentMedia = minimumListenedTimeForMedia != null)
        listenedMediaObserver.setMinimumListenedTimeForMedia(minimumListenedTimeForMedia)
    }

    override fun finish() {
        listenedMediaObserver.finish()
    }

    companion object {
        private val minimumAdListenTime = 5.0.toDuration(DurationUnit.Seconds)
    }
}