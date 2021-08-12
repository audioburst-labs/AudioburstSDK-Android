package com.audioburst.player.core.media

import kotlinx.coroutines.flow.MutableStateFlow

internal class PlaybackNotificationController {

    val shouldDisplayNotification = MutableStateFlow(true)
}