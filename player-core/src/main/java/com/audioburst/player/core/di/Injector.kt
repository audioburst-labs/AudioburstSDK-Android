package com.audioburst.player.core.di

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import com.audioburst.library.AudioburstLibrary
import com.audioburst.player.core.AudioburstPlayerCore
import com.audioburst.player.core.data.AdUrlRepository
import com.audioburst.player.core.data.AudioburstLibraryRepository
import com.audioburst.player.core.di.provider.Provider
import com.audioburst.player.core.di.provider.provider
import com.audioburst.player.core.di.provider.singleton
import com.audioburst.player.core.interactors.GetAdvertisementUrl
import com.audioburst.player.core.interactors.GetAdvertisementUrlInteractor
import com.audioburst.player.core.media.*
import com.audioburst.player.core.models.AppDispatchers
import com.audioburst.player.core.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.properties.Delegates

internal object Injector {

    private var applicationContext: Context by Delegates.notNull()
    private var applicationKey: String by Delegates.notNull()
    private val appDispatchersProvider: Provider<AppDispatchers> = provider {
        AppDispatchers(
            background = Dispatchers.Default,
            main = Dispatchers.Main
        )
    }
    private val audioburstLibraryProvider: Provider<AudioburstLibrary> = provider {
        AudioburstLibrary(applicationKey = applicationKey)
    }
    private val adUrlRepositoryProvider: Provider<AdUrlRepository> = provider {
        AudioburstLibraryRepository(
            audioburstLibrary = audioburstLibraryProvider.get(),
        )
    }
    private val adUrlCacheProvider: Provider<AdUrlCache> = singleton { InMemoryAdUrlCache() }
    private val getAdvertisementUrlProvider: Provider<GetAdvertisementUrl> = provider {
        GetAdvertisementUrlInteractor(
            adUrlCache = adUrlCacheProvider.get(),
            adUrlRepository = adUrlRepositoryProvider.get(),
        )
    }
    private val inMemoryCurrentPlaylistCacheProvider: Provider<InMemoryCurrentPlaylistCache> = singleton { InMemoryCurrentPlaylistCache() }
    private val currentPlaylistCacheProvider: Provider<CurrentPlaylistCache> = provider { inMemoryCurrentPlaylistCacheProvider.get() }
    private val currentPlaylistCacheSetterProvider: Provider<CurrentPlaylistCacheSetter> = provider { inMemoryCurrentPlaylistCacheProvider.get() }
    private val mediaSourceModuleProvider: Provider<MediaSourceModule> = singleton {
        MediaSourceModule(
            context = applicationContext,
            adUriResolverProvider = adUriResolverProvider,
            downloadOnlyInterceptorProvider = downloadOnlyInterceptorProvider,
        )
    }
    private val adUriResolverProvider: Provider<AdUriResolver> = provider {
        AdUriResolver(
            currentPlaylistCache = currentPlaylistCacheProvider.get(),
            getAdvertisementUrl = getAdvertisementUrlProvider.get(),
        )
    }
    private val mediaUrlValidatorProvider: Provider<MediaUrlValidator> = provider { ExtensionBasedMediaUrlValidator() }
    private val downloadOnlyInterceptorProvider: Provider<DownloadOnlyInterceptor> = provider {
        DownloadOnlyInterceptor(mediaUrlValidator = mediaUrlValidatorProvider.get())
    }
    private val mediaBrowserConnectionCallbackProvider: Provider<MediaBrowserConnectionCallback> = singleton { MediaBrowserConnectionCallback() }
    private val mediaBrowserCompatProvider: Provider<MediaBrowserCompat> = provider {
        MediaBrowserCompat(
            applicationContext,
            ComponentName(applicationContext, MediaService::class.java),
            mediaBrowserConnectionCallbackProvider.get(),
            null
        )
    }
    private val mediaSessionConnectionProvider: Provider<MediaSessionConnection> = singleton {
        MediaSessionConnection(mediaBrowser = mediaBrowserCompatProvider.get())
    }
    private val burstPlayerDelegate: Provider<BurstPlayerDelegate> = singleton {
        BurstPlayerDelegate(
            mainDispatcher = appDispatchersProvider.get().main,
            mediaSessionConnection = mediaSessionConnectionProvider.get(),
        )
    }
    private val playbackNotificationControllerProvider: Provider<PlaybackNotificationController> = singleton {
        PlaybackNotificationController()
    }
    private val notificationBuilderFactoryProvider: Provider<NotificationBuilder.Factory> = provider {
        NotificationBuilder.Factory(context = applicationContext)
    }

    fun init(context: Context, applicationKey: String) {
        applicationContext = context.applicationContext
        Injector.applicationKey = applicationKey
    }

    fun inject(mediaService: MediaService) {
        val serviceScope = CoroutineScope(context = appDispatchersProvider.get().main + SupervisorJob())
        val mediaModule = MediaModule(
            context = applicationContext,
            serviceScope = serviceScope,
            appDispatchersProvider = appDispatchersProvider,
            mediaBrowserConnectionCallbackProvider = mediaBrowserConnectionCallbackProvider,
            adUrlCacheProvider = adUrlCacheProvider,
            currentPlaylistCacheSetterProvider = currentPlaylistCacheSetterProvider,
            mediaSourceModuleProvider = mediaSourceModuleProvider,
        )
        burstPlayerDelegate.get().setBurstPlayer(mediaModule.burstExoPlayerProvider.get())
        mediaService.apply {
            scope = serviceScope
            exoPlayer = mediaModule.exoPlayerProvider.get()
            burstPlayer = mediaModule.burstExoPlayerProvider.get()
            audioburstLibrary = audioburstLibraryProvider.get()
            mediaControllerCallback = mediaModule.mediaControllerCallbackProvider.get()
            notificationBuilderFactory = notificationBuilderFactoryProvider.get()
            playbackNotificationController = playbackNotificationControllerProvider.get()
            playerEventFlow = mediaModule.playerEventFlowProvider.get()
        }
    }

    fun inject(player: AudioburstPlayerCore) {
        player.apply {
            mediaSessionConnection = mediaSessionConnectionProvider.get()
            playbackNotificationController = playbackNotificationControllerProvider.get()
            _audioburstLibrary = audioburstLibraryProvider.get()
            _burstPlayer = burstPlayerDelegate.get()
        }
    }
}