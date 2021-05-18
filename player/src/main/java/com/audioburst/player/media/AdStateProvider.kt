package com.audioburst.player.media

import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.models.AdState
import com.audioburst.player.models.MediaUrl
import com.audioburst.player.models.NowPlaying
import com.audioburst.player.utils.ListenedMediaObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface AdStateProvider {

    val adState: StateFlow<AdState?>

    fun onNowPlaying(nowPlaying: NowPlaying)

    fun finish()
}

internal class TimeAwareAdStateProvider(
    private val listenedMediaObserver: ListenedMediaObserver,
) : AdStateProvider {

    private var currentMedia: NowPlaying.Media? = null

    private val _adState = MutableStateFlow<AdState?>(null)
    override val adState: StateFlow<AdState?>
        get() = _adState.asStateFlow()

    init {
        listenedMediaObserver.onListenedObserver = {
            if (_adState.value?.isAvailableInCurrentMedia == true) {
                _adState.value = _adState.value?.copy(canSkip = true)
            }
        }
    }

    override fun onNowPlaying(nowPlaying: NowPlaying) {
        when (nowPlaying) {
            is NowPlaying.Media -> onMedia(nowPlaying)
            is NowPlaying.Nothing -> {
                _adState.value = null
                currentMedia = null
            }
        }
    }

    private fun onMedia(media: NowPlaying.Media) {
        if (currentMedia?.burst == media.burst && currentMedia?.mediaUrl == media.mediaUrl) {
            return
        }
        currentMedia = media
        val minimumListenedTimeForMedia = if (media.mediaUrl is MediaUrl.Advertisement) minimumAdListenTime else null
        _adState.value = AdState(isAvailableInCurrentMedia = minimumListenedTimeForMedia != null)
        listenedMediaObserver.setMinimumListenedTimeForMedia(minimumListenedTimeForMedia)
    }

    override fun finish() {
        listenedMediaObserver.finish()
    }

    companion object {
        private val minimumAdListenTime = 5.0.toDuration(DurationUnit.Seconds)
    }
}