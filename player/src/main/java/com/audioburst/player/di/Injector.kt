package com.audioburst.player.di

import android.content.Context
import com.audioburst.library.AudioburstLibrary
import com.audioburst.player.AudioburstPlayerCore
import com.audioburst.player.data.AdUrlRepository
import com.audioburst.player.data.AudioburstLibraryRepository
import com.audioburst.player.di.provider.Provider
import com.audioburst.player.di.provider.provider
import com.audioburst.player.di.provider.singleton
import com.audioburst.player.media.MediaService
import com.audioburst.player.models.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.properties.Delegates

internal object Injector {

    private var applicationContext: Context by Delegates.notNull()
    private var applicationKey: String by Delegates.notNull()
    private val mediaModule: MediaModule by lazy {
        MediaModule(
            context = applicationContext,
            libraryScopeProvider = libraryScopeProvider,
            appDispatchersProvider = appDispatchersProvider,
            adUrlRepositoryProvider = adUrlRepositoryProvider,
        )
    }
    private val libraryScopeProvider: Provider<CoroutineScope> = singleton {
        CoroutineScope(context = appDispatchersProvider.get().main + SupervisorJob())
    }
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

    fun init(context: Context, applicationKey: String) {
        this.applicationContext = context.applicationContext
        this.applicationKey = applicationKey
    }

    fun inject(mediaService: MediaService) {
        mediaService.apply {
            scope = libraryScopeProvider.get()
            exoPlayer = mediaModule.exoPlayerProvider.get()
            burstPlayer = mediaModule.burstExoPlayerProvider.get()
            audioburstLibrary = audioburstLibraryProvider.get()
            mediaControllerCallback = mediaModule.mediaControllerCallbackProvider.get()
        }
    }

    fun inject(player: AudioburstPlayerCore) {
        player.apply {
            mediaSessionConnection = mediaModule.mediaSessionConnectionProvider.get()
            _audioburstLibrary = audioburstLibraryProvider.get()
            _burstPlayer = mediaModule.burstPlayerProvider.get()
        }
    }
}