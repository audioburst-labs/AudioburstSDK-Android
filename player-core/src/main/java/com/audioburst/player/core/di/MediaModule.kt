package com.audioburst.player.core.di

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import com.audioburst.player.core.utils.*
import com.audioburst.player.core.utils.AnalyticCollectorMediaTotalPlayTimeProvider
import com.audioburst.player.core.utils.ExtensionBasedMediaUrlValidator
import com.audioburst.player.core.utils.ListenedMediaObserver
import com.audioburst.player.core.utils.MediaTotalPlayTimeProvider
import com.audioburst.player.core.utils.MediaUrlValidator
import com.audioburst.player.core.utils.TimeBasedListenedMediaObserver
import com.audioburst.player.core.data.AdUrlRepository
import com.audioburst.player.core.di.provider.Provider
import com.audioburst.player.core.di.provider.provider
import com.audioburst.player.core.di.provider.singleton
import com.audioburst.player.core.interactors.GetAdvertisementUrl
import com.audioburst.player.core.interactors.GetAdvertisementUrlInteractor
import com.audioburst.player.core.media.*
import com.audioburst.player.core.media.MediaBrowserConnectionCallback
import com.audioburst.player.core.media.MediaControllerCallback
import com.audioburst.player.core.media.MediaPlayer
import com.audioburst.player.core.media.MediaService
import com.audioburst.player.core.media.MediaSessionConnection
import com.audioburst.player.core.media.events.ExoPlayerEventsFlow
import com.audioburst.player.core.media.events.PlayerEventFlow
import com.audioburst.player.core.media.mappers.BurstToMediaItemMapper
import com.audioburst.player.core.models.AppDispatchers
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

internal class MediaModule(
    context: Context,
    adUrlRepositoryProvider: Provider<AdUrlRepository>,
    libraryScopeProvider: Provider<CoroutineScope>,
    appDispatchersProvider: Provider<AppDispatchers>,
) {

    private val mediaSourceModuleProvider: MediaSourceModule by lazy {
        MediaSourceModule(
            context = context,
            adUriResolverProvider = adUriResolverProvider,
            downloadOnlyInterceptorProvider = downloadOnlyInterceptorProvider,
        )
    }

    private val analyticsCollectorProvider: Provider<AnalyticsCollector> = singleton { AnalyticsCollector(Clock.DEFAULT) }
    private val mediaTotalPlayTimeProvider: Provider<MediaTotalPlayTimeProvider> = provider {
        AnalyticCollectorMediaTotalPlayTimeProvider(analyticsCollector = analyticsCollectorProvider.get())
    }
    private val adUriResolverProvider: Provider<AdUriResolver> = provider {
        AdUriResolver(
            currentPlaylistCache = currentPlaylistCacheProvider.get(),
            getAdvertisementUrl = getAdvertisementUrlProvider.get(),
        )
    }
    private val downloadOnlyInterceptorProvider: Provider<DownloadOnlyInterceptor> = provider {
        DownloadOnlyInterceptor(mediaUrlValidator = mediaUrlValidatorProvider.get())
    }
    private val getAdvertisementUrlProvider: Provider<GetAdvertisementUrl> = provider {
        GetAdvertisementUrlInteractor(
            adUrlCache = adUrlCacheProvider.get(),
            adUrlRepository = adUrlRepositoryProvider.get(),
        )
    }
    private val playerEventFlowProvider: Provider<PlayerEventFlow> = provider {
        ExoPlayerEventsFlow(
            exoPlayer = exoPlayerProvider.get(),
            appDispatchers = appDispatchersProvider.get(),
        )
    }
    private val burstToMediaItemMapperProvider: Provider<BurstToMediaItemMapper> = provider { BurstToMediaItemMapper() }
    private val burstDownloaderProvider: Provider<BurstDownloader> = provider { NoOpBurstDownloader() }
    private val mediaBrowserConnectionCallbackProvider: Provider<MediaBrowserConnectionCallback> = singleton { MediaBrowserConnectionCallback() }
    private val mediaPlayerProvider: Provider<MediaPlayer> = singleton {
        ExoMediaPlayer(
            scope = libraryScopeProvider.get(),
            exoPlayer = exoPlayerProvider.get(),
            burstDownloader = burstDownloaderProvider.get(),
            mediaControllerCallback = mediaControllerCallbackProvider.get(),
            playerEventFlow = playerEventFlowProvider.get(),
        )
    }
    private val playingAwareTimerCreatorProvider: Provider<PlayingAwareTimer.Creator> = provider {
        PlayingAwareTimer.Creator(
            scope = libraryScopeProvider.get(),
            playerEventFlow = playerEventFlowProvider.get(),
        )
    }
    private val listenedMediaObserverProvider: Provider<ListenedMediaObserver> = provider {
        TimeBasedListenedMediaObserver(
            scope = libraryScopeProvider.get(),
            playingAwareTimerCreator = playingAwareTimerCreatorProvider.get(),
            mediaTotalPlayTimeProvider = mediaTotalPlayTimeProvider.get(),
        )
    }
    private val adStateProviderProvider: Provider<AdStateProvider> = provider {
        TimeAwareAdStateProvider(listenedMediaObserver = listenedMediaObserverProvider.get())
    }
    private val mediaUrlValidatorProvider: Provider<MediaUrlValidator> = provider { ExtensionBasedMediaUrlValidator() }
    private val componentNameProvider: Provider<ComponentName> = provider { ComponentName(context, MediaService::class.java) }
    private val adUrlCacheProvider: Provider<AdUrlCache> = singleton { InMemoryAdUrlCache() }
    private val mediaBrowserCompatProvider: Provider<MediaBrowserCompat> = provider {
        MediaBrowserCompat(context, componentNameProvider.get(), mediaBrowserConnectionCallbackProvider.get(), null)
    }
    private val inMemoryCurrentPlaylistCacheProvider: Provider<InMemoryCurrentPlaylistCache> = singleton { InMemoryCurrentPlaylistCache() }
    private val currentPlaylistCacheProvider: Provider<CurrentPlaylistCache> = provider { inMemoryCurrentPlaylistCacheProvider.get() }
    private val currentPlaylistCacheSetterProvider: Provider<CurrentPlaylistCacheSetter> = provider { inMemoryCurrentPlaylistCacheProvider.get() }
    private val playbackTimerCreatorProvider: Provider<PlaybackTimer.Creator> = provider {
        PlaybackTimer.Creator(
            scope = libraryScopeProvider.get(),
            playingAwareTimerCreator = playingAwareTimerCreatorProvider.get(),
            mediaPlayer = mediaPlayerProvider.get(),
        )
    }
    val mediaControllerCallbackProvider: Provider<MediaControllerCallback> = singleton {
        MediaControllerCallback(mediaBrowserConnectionCallbackProvider.get())
    }
    val burstPlayerProvider: Provider<BurstPlayer> = singleton { burstExoPlayerProvider.get() }
    val burstExoPlayerProvider: Provider<BurstExoPlayer> = singleton {
        BurstExoPlayer(
            playerEventFlow = playerEventFlowProvider.get(),
            scope = libraryScopeProvider.get(),
            burstToMediaItemMapper = burstToMediaItemMapperProvider.get(),
            mediaPlayer = mediaPlayerProvider.get(),
            adStateProvider = adStateProviderProvider.get(),
            currentPlaylistCacheSetter = currentPlaylistCacheSetterProvider.get(),
            adUrlCache = adUrlCacheProvider.get(),
            playbackTimerCreator = playbackTimerCreatorProvider.get(),
        )
    }
    val mediaSessionConnectionProvider: Provider<MediaSessionConnection> = singleton {
        MediaSessionConnection(mediaBrowser = mediaBrowserCompatProvider.get())
    }
    val exoPlayerProvider: Provider<ExoPlayer> = singleton {
        val audioOnlyRenderersFactory = RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, handler, audioListener)
            )
        }
        SimpleExoPlayer.Builder(context, audioOnlyRenderersFactory, ExtractorsFactory.EMPTY)
            .setAnalyticsCollector(analyticsCollectorProvider.get())
            .setMediaSourceFactory(mediaSourceModuleProvider.mediaSourceFactoryProvider.get())
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