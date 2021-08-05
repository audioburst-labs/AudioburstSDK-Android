package com.audioburst.player.controller.models

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.audioburst.player.controller.R
import com.audioburst.player.controller.utils.ResourceProvider

public sealed class Color {

    internal class Resource(val color: Int): Color()

    internal class Raw(val color: Int): Color()

    public companion object {

        public val DEFAULT: Color = Resource(R.color.audioburst_player_controller_default_color)
    }
}

@ColorInt
public fun Color.colorInt(context: Context): Int =
    when (this) {
        is Color.Resource -> ContextCompat.getColor(context, color)
        is Color.Raw -> color
    }

@ColorInt
public fun Color.colorInt(resourceProvider: ResourceProvider): Int =
    when (this) {
        is Color.Resource -> resourceProvider.getColor(color)
        is Color.Raw -> color
    }