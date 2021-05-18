package com.audioburst.player.utils

import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.extensions.duration
import com.audioburst.player.media.MediaPlayer
import com.audioburst.player.models.PlaybackTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

internal class PlaybackTimer private constructor(
    scope: CoroutineScope,
    updateInterval: Duration,
    playingAwareTimerCreator: PlayingAwareTimer.Creator,
    private val mediaPlayer: MediaPlayer,
) {

    private val timer = playingAwareTimerCreator.create(updateInterval)
    private val _playbackTime = MutableStateFlow(currentPlaybackTime())
    val playbackTime = _playbackTime.asStateFlow()

    init {
        timer.timer
            .combine(mediaPlayer.nowPlaying) { _, _ -> currentPlaybackTime() }
            .onEach { _playbackTime.value = it }
            .launchIn(scope)
    }

    private fun currentPlaybackTime(): PlaybackTime =
        PlaybackTime(
            playbackPosition = mediaPlayer.playbackPosition.milliseconds,
            mediaDuration = mediaPlayer.nowPlaying.value.duration.milliseconds,
        )

    private val Long.milliseconds: Duration
        get() = toDouble().toDuration(DurationUnit.Milliseconds)

    class Creator(
        private val scope: CoroutineScope,
        private val playingAwareTimerCreator: PlayingAwareTimer.Creator,
        private val mediaPlayer: MediaPlayer,
    ) {
        fun create(updateInterval: Duration): PlaybackTimer =
            PlaybackTimer(
                scope = scope,
                mediaPlayer = mediaPlayer,
                updateInterval = updateInterval,
                playingAwareTimerCreator = playingAwareTimerCreator,
            )
    }
}