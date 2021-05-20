package com.audioburst.player.core.models

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal fun appDispatchersOf(
    background: CoroutineDispatcher = Dispatchers.Unconfined,
    main: CoroutineDispatcher = Dispatchers.Unconfined,
): AppDispatchers =
    AppDispatchers(
        background = background,
        main = main,
    )