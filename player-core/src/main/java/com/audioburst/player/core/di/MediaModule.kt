package com.audioburst.player.core.di

import android.content.Context
import com.audioburst.player.core.di.provider.Provider
import com.audioburst.player.core.di.provider.provider
import com.audioburst.player.core.di.provider.singleton
import com.audioburst.player.core.media.*
import com.audioburst.player.core.media.events.ExoPlayerEventsFlow
import com.audioburst.player.core.media.events.PlayerEventFlow
import com.audioburst.player.core.media.mappers.BurstToMediaItemMapper
import com.audioburst.player.core.models.AppDispatchers
import com.audioburst.player.core.utils.*
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.util.Clock
import kotlinx.coroutines.CoroutineScope

/**
 * New instance of this class is being created every time new instance of the [MediaService] is created.
 * This effectively means that all of the classes instantiated here are scoped to the [MediaService].
 */
internal class MediaModule(
    context: Context,
    serviceScope: CoroutineScope,
    appDispatchersProvider: Provider<AppDispatchers>,
    mediaBrowserConnectionCallbackProvider: Provider<MediaBrowserConnectionCallback>,
    private val mediaSourceModuleProvider: Provider<MediaSourceModule>,
    private val adUrlCacheProvider: Provider<AdUrlCache>,
    private val currentPlaylistCacheSetterProvider: Provider<CurrentPlaylistCacheSetter>,
) {

    private val analyticsCollectorProvider: Provider<AnalyticsCollector> = singleton { AnalyticsCollector(Clock.DEFAULT) }
    private val mediaTotalPlayTimeProvider: Provider<MediaTotalPlayTimeProvider> = provider {
        AnalyticCollectorMediaTotalPlayTimeProvider(analyticsCollector = analyticsCollectorProvider.get())
    }
    val playerEventFlowProvider: Provider<PlayerEventFlow> = provider {
        ExoPlayerEventsFlow(
            exoPlayer = exoPlayerProvider.get(),
            appDispatchers = appDispatchersProvider.get(),
        )
    }
    private val burstToMediaItemMapperProvider: Provider<BurstToMediaItemMapper> = provider { BurstToMediaItemMapper() }
    private val burstDownloaderProvider: Provider<BurstDownloader> = provider { NoOpBurstDownloader() }
    private val mediaPlayerProvider: Provider<MediaPlayer> = singleton {
        ExoMediaPlayer(
            scope = serviceScope,
            exoPlayer = exoPlayerProvider.get(),
            burstDownloader = burstDownloaderProvider.get(),
            mediaControllerCallback = mediaControllerCallbackProvider.get(),
            playerEventFlow = playerEventFlowProvider.get(),
        )
    }
    private val playingAwareTimerCreatorProvider: Provider<PlayingAwareTimer.Creator> = provider {
        PlayingAwareTimer.Creator(
            scope = serviceScope,
            playerEventFlow = playerEventFlowProvider.get(),
        )
    }
    private val listenedMediaObserverProvider: Provider<ListenedMediaObserver> = provider {
        TimeBasedListenedMediaObserver(
            scope = serviceScope,
            playingAwareTimerCreator = playingAwareTimerCreatorProvider.get(),
            mediaTotalPlayTimeProvider = mediaTotalPlayTimeProvider.get(),
        )
    }
    private val adStateProviderProvider: Provider<AdStateProvider> = provider {
        TimeAwareAdStateProvider(listenedMediaObserver = listenedMediaObserverProvider.get())
    }
    private val playbackTimerCreatorProvider: Provider<PlaybackTimer.Creator> = provider {
        PlaybackTimer.Creator(
            scope = serviceScope,
            playingAwareTimerCreator = playingAwareTimerCreatorProvider.get(),
            mediaPlayer = mediaPlayerProvider.get(),
        )
    }
    val mediaControllerCallbackProvider: Provider<MediaControllerCallback> = singleton {
        MediaControllerCallback(mediaBrowserConnectionCallbackProvider.get())
    }
    val burstExoPlayerProvider: Provider<BurstExoPlayer> = singleton {
        BurstExoPlayer(
            playerEventFlow = playerEventFlowProvider.get(),
            scope = serviceScope,
            burstToMediaItemMapper = burstToMediaItemMapperProvider.get(),
            mediaPlayer = mediaPlayerProvider.get(),
            adStateProvider = adStateProviderProvider.get(),
            currentPlaylistCacheSetter = currentPlaylistCacheSetterProvider.get(),
            adUrlCache = adUrlCacheProvider.get(),
            playbackTimerCreator = playbackTimerCreatorProvider.get(),
        )
    }
    val exoPlayerProvider: Provider<ExoPlayer> = singleton {
        val audioOnlyRenderersFactory = RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, handler, audioListener)
            )
        }
        SimpleExoPlayer.Builder(context, audioOnlyRenderersFactory, ExtractorsFactory.EMPTY)
            .setAnalyticsCollector(analyticsCollectorProvider.get())
            .setMediaSourceFactory(mediaSourceModuleProvider.get().mediaSourceFactoryProvider.get())
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(), true
                )
            }
    }
}