package com.audioburst.player.controller.utils

import com.audioburst.player.controller.R
import kotlin.math.floor

internal object DateUtils {

    fun formatAsTime(value: Long, resourceProvider: ResourceProvider): String {
        val totalSeconds = floor(value / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)
        return if (value < 0) resourceProvider.getString(R.string.audioburst_player_controller_duration_unknown)
        else resourceProvider.getString(R.string.audioburst_player_controller_duration_format).format(minutes, remainingSeconds)
    }
}