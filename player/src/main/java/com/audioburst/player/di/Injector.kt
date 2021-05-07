package com.audioburst.player.di

import android.content.Context
import com.audioburst.player.Player
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
    private val mediaModule: MediaModule by lazy {
        MediaModule(
            context = applicationContext,
            libraryScopeProvider = libraryScopeProvider,
            appDispatchersProvider = appDispatchersProvider,
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

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun inject(mediaService: MediaService) {
        mediaService.apply {
            scope = libraryScopeProvider.get()
            exoPlayer = mediaModule.exoPlayerProvider.get()
            burstPlayer = mediaModule.burstPlayerProvider.get()
            mediaControllerCallback = mediaModule.mediaControllerCallbackProvider.get()
        }
    }

    fun inject(player: Player) {
        player.apply {
            mediaSessionConnection = mediaModule.mediaSessionConnectionProvider.get()
            burstPlayer = mediaModule.burstPlayerProvider.get()
        }
    }
}