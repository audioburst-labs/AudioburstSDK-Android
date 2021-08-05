package com.audioburst.player.controller.models

import androidx.annotation.StringRes
import com.audioburst.player.controller.R

public enum class SwitchSourceButton(@StringRes public val text: Int) {
    FullShow(R.string.audioburst_player_controller_switch_source_button_full_show),
    GoBack(R.string.audioburst_player_controller_switch_source_button_go_back),
}