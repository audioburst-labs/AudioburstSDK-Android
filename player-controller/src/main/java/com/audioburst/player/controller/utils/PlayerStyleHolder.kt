package com.audioburst.player.controller.utils

import com.audioburst.player.controller.models.PlayerTheme
import com.audioburst.player.controller.models.UiMode
import com.audioburst.player.controller.models.PlayerStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface PlayerStyleHolder {

    val playerStyle: StateFlow<PlayerStyle>
}

internal class InMemoryPlayerStyleHolder(
    resourceProvider: ResourceProvider
) : PlayerStyleHolder {

    private val _playerStyle = MutableStateFlow(
        PlayerStyle(
            playerTheme = when (resourceProvider.getUiMode()) {
                UiMode.NightNo -> PlayerTheme.Light
                else -> PlayerTheme.Dark
            }
        )
    )

    override val playerStyle: StateFlow<PlayerStyle> = _playerStyle.asStateFlow()
}