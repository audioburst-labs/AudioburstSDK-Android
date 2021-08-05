package com.audioburst.player.controller.di

import android.content.Context
import com.audioburst.player.controller.AudioburstPlayerController
import com.audioburst.player.controller.BurstPlayerController
import com.audioburst.player.controller.PlayerController
import com.audioburst.player.controller.di.provider.Provider
import com.audioburst.player.controller.di.provider.provider
import com.audioburst.player.controller.di.provider.singleton
import com.audioburst.player.controller.utils.*
import com.audioburst.player.controller.utils.AndroidResourceProvider
import com.audioburst.player.controller.utils.InMemoryListenedBurstIdsHolder
import com.audioburst.player.controller.utils.ListenedBurstIds
import com.audioburst.player.controller.utils.PlayerStyleHolder
import com.audioburst.player.core.AudioburstPlayerCore
import com.audioburst.player.core.media.BurstPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.properties.Delegates

internal object Injector {

    private var applicationContext: Context by Delegates.notNull()
    private val libraryScope = CoroutineScope(context = Dispatchers.Main.immediate + SupervisorJob())

    private val playerStyleHolderProvider: Provider<PlayerStyleHolder> = singleton {
        InMemoryPlayerStyleHolder(
            resourceProvider = resourceProviderProvider.get()
        )
    }
    private val listenedBurstIdsProvider: Provider<ListenedBurstIds> = singleton { InMemoryListenedBurstIdsHolder() }
    private val resourceProviderProvider: Provider<ResourceProvider> = provider { AndroidResourceProvider(applicationContext) }
    private val libraryScopeProvider: Provider<CoroutineScope> = provider { libraryScope }
    private val burstPlayerProvider: Provider<BurstPlayer> = provider { AudioburstPlayerCore.burstPlayer }
    private val playerControllerProvider: Provider<PlayerController> = provider {
        BurstPlayerController(
            burstPlayer = burstPlayerProvider.get(),
            scope = libraryScopeProvider.get(),
            resourceProvider = resourceProviderProvider.get(),
            listenedBurstIds = listenedBurstIdsProvider.get(),
            playerStyleHolder = playerStyleHolderProvider.get(),
        )
    }

    fun init(context: Context) {
        this.applicationContext = context.applicationContext
    }

    fun inject(audioburstPlayerController: AudioburstPlayerController) {
        audioburstPlayerController.apply {
            _playerController = playerControllerProvider.get()
        }
    }
}