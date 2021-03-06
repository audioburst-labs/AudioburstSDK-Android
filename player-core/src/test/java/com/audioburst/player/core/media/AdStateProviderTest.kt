package com.audioburst.player.core.media

import com.audioburst.library.models.Burst
import com.audioburst.library.models.Duration
import com.audioburst.library.models.DurationUnit
import com.audioburst.library.models.toDuration
import com.audioburst.player.core.models.AdState
import com.audioburst.player.core.models.MediaUrl
import com.audioburst.player.core.models.NowPlaying
import com.audioburst.player.core.models.burstOf
import com.audioburst.player.core.utils.ListenedMediaObserver
import org.junit.Test

internal class AdStateProviderTest {

    @Test
    fun `test if there is null emitted when there is no Media currently playing`() {
        // GIVEN
        // WHEN
        val adStateProvider = timeAwareAdStateProviderOf(listenedMediaObserverOf())

        // THEN
        assert(adStateProvider.adState.value == null)
    }

    @Test
    fun `test if there is null emitted when current NowPlaying is Nothing`() {
        // GIVEN
        val nowPlaying = NowPlaying.Nothing()

        // WHEN
        val adStateProvider = timeAwareAdStateProviderOf(listenedMediaObserverOf())
        adStateProvider.onNowPlaying(nowPlaying)

        // THEN
        assert(adStateProvider.adState.value == null)
    }

    @Test
    fun `test if there is not null State emitted with isAvailableInCurrentMedia = false when current NowPlaying is new Media with Burst MediaUrl`() {
        // GIVEN
        val nowPlaying = mediaOf(mediaUrl = MediaUrl.Burst(""))

        // WHEN
        val adStateProvider = timeAwareAdStateProviderOf(listenedMediaObserverOf())
        adStateProvider.onNowPlaying(nowPlaying)

        // THEN
        assert(adStateProvider.adState.value == AdState(isAvailableInCurrentMedia = false))
    }

    @Test
    fun `test if there is not null State emitted with isAvailableInCurrentMedia = false when current NowPlaying is new Media with Source MediaUrl`() {
        // GIVEN
        val nowPlaying = mediaOf(mediaUrl = MediaUrl.Source(""))

        // WHEN
        val adStateProvider = timeAwareAdStateProviderOf(listenedMediaObserverOf())
        adStateProvider.onNowPlaying(nowPlaying)

        // THEN
        assert(adStateProvider.adState.value == AdState(isAvailableInCurrentMedia = false))
    }

    @Test
    fun `test if there is not null State emitted with isAvailableInCurrentMedia = true and canSkip = false when current NowPlaying is new Media with Advertisement MediaUrl`() {
        // GIVEN
        val nowPlaying = mediaOf(mediaUrl = MediaUrl.Advertisement(""))

        // WHEN
        val adStateProvider = timeAwareAdStateProviderOf(listenedMediaObserverOf())
        adStateProvider.onNowPlaying(nowPlaying)

        // THEN
        assert(adStateProvider.adState.value == AdState(isAvailableInCurrentMedia = true, canSkip = false))
    }

    @Test
    fun `test if there is not null State emitted with isAvailableInCurrentMedia = true and canSkip = true when current NowPlaying is new Media with Advertisement MediaUrl and onListenedObserver is being called`() {
        // GIVEN
        val nowPlaying = mediaOf(mediaUrl = MediaUrl.Advertisement(""))

        // WHEN
        val listenedMediaObserver = listenedMediaObserverOf()
        val adStateProvider = timeAwareAdStateProviderOf(listenedMediaObserver)
        adStateProvider.onNowPlaying(nowPlaying)
        listenedMediaObserver.onListenedObserver?.invoke()

        // THEN
        assert(adStateProvider.adState.value == AdState(isAvailableInCurrentMedia = true, canSkip = true))
    }
}

internal fun mediaOf(
    burst: Burst = burstOf(),
    mediaUrl: MediaUrl = MediaUrl.Burst(""),
    positionInPlaylist: Int = 0,
    duration: Long = 0L,
): NowPlaying.Media =
    NowPlaying.Media(
        burst = burst,
        mediaUrl = mediaUrl,
        positionInPlaylist = positionInPlaylist,
        duration = duration.toDouble().toDuration(DurationUnit.Milliseconds),
    )

internal fun timeAwareAdStateProviderOf(listenedMediaObserver: ListenedMediaObserver): TimeAwareAdStateProvider =
    TimeAwareAdStateProvider(listenedMediaObserver = listenedMediaObserver)

internal fun listenedMediaObserverOf(): ListenedMediaObserver =
    object : ListenedMediaObserver {
        override var onListenedObserver: (() -> Unit)? = null
        override fun setMinimumListenedTimeForMedia(minimumListenedTimeForMedia: Duration?) = Unit
        override fun finish() = Unit
    }