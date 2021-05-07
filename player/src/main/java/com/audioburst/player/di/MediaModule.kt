package com.audioburst.player.di

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import com.audioburst.player.di.provider.Provider
import com.audioburst.player.di.provider.provider
import com.audioburst.player.di.provider.singleton
import com.audioburst.player.interactors.GetAdvertisementUrl
import com.audioburst.player.interactors.NoOpGetAdvertisementUrl
import com.audioburst.player.media.*
import com.audioburst.player.media.events.PlayerEventFlow
import com.audioburst.player.media.mappers.BurstToMediaItem
import com.audioburst.player.media.mappers.MediaItemToExoPlayerMediaItemMapper
import com.audioburst.player.models.AppDispatchers
import com.audioburst.player.utils.DownloadOnlyInterceptor
import com.audioburst.player.utils.ExtensionBasedMediaUrlValidator
import com.audioburst.player.utils.MediaUrlValidator
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
    libraryScopeProvider: Provider<CoroutineScope>,
    appDispatchersProvider: Provider<AppDispatchers>,
) {

    private val analyticsCollectorProvider: Provider<AnalyticsCollector> = singleton { AnalyticsCollector(Clock.DEFAULT) }
    private val adUriResolverProvider: Provider<AdUriResolver> = provider {
        AdUriResolver(
            burstPlayerProvider = burstPlayerProvider,
            mediaUrlValidator = mediaUrlValidatorProvider.get(),
            getAdvertisementUrl = getAdvertisementUrlProvider.get(),
        )
    }
    private val downloadOnlyInterceptorProvider: Provider<DownloadOnlyInterceptor> = provider {
        DownloadOnlyInterceptor(mediaUrlValidator = mediaUrlValidatorProvider.get())
    }
    private val mediaSourceModuleProvider: Provider<MediaSourceModule> = provider {
        MediaSourceModule(
            context = context,
            adUriResolverProvider = adUriResolverProvider,
            downloadOnlyInterceptorProvider = downloadOnlyInterceptorProvider,
        )
    }
    private val getAdvertisementUrlProvider: Provider<GetAdvertisementUrl> = provider { NoOpGetAdvertisementUrl() }
    private val playerEventFlowProvider: Provider<PlayerEventFlow> = provider {
        PlayerEventFlow(
            exoPlayer = exoPlayerProvider.get(),
            appDispatchers = appDispatchersProvider.get(),
        )
    }
    private val burstToMediaItemProvider: Provider<BurstToMediaItem> = provider { BurstToMediaItem() }
    private val mediaItemToExoPlayerMediaItemMapperProvider: Provider<MediaItemToExoPlayerMediaItemMapper> = provider { MediaItemToExoPlayerMediaItemMapper() }
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
    private val adStateProviderProvider: Provider<AdStateProvider> = provider { NoOpAdStateProvider() }
    private val mediaUrlValidatorProvider: Provider<MediaUrlValidator> = provider { ExtensionBasedMediaUrlValidator() }
    private val componentNameProvider: Provider<ComponentName> = provider { ComponentName(context, MediaService::class.java) }
    val mediaControllerCallbackProvider: Provider<MediaControllerCallback> = singleton {
        MediaControllerCallback(mediaBrowserConnectionCallbackProvider.get())
    }
    val burstPlayerProvider: Provider<BurstPlayer> = singleton {
        BurstExoPlayer(
            playerEventFlow = playerEventFlowProvider.get(),
            scope = libraryScopeProvider.get(),
            burstToMediaItem = burstToMediaItemProvider.get(),
            mediaItemToExoPlayerMediaItemMapper = mediaItemToExoPlayerMediaItemMapperProvider.get(),
            mediaPlayer = mediaPlayerProvider.get(),
            adStateProvider = adStateProviderProvider.get(),
        )
    }
    private val mediaBrowserCompatProvider: Provider<MediaBrowserCompat> = provider {
        MediaBrowserCompat(context, componentNameProvider.get(), mediaBrowserConnectionCallbackProvider.get(), null)
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