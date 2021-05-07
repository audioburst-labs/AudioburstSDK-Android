package com.audioburst.player.models

import kotlinx.coroutines.CoroutineDispatcher

internal data class AppDispatchers(
    val background: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)