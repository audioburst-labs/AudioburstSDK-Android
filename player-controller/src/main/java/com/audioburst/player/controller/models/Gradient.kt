package com.audioburst.player.controller.models

import android.content.Context
import android.graphics.drawable.GradientDrawable
import com.audioburst.player.controller.R
import com.audioburst.player.controller.models.Color
import com.audioburst.player.controller.models.colorInt

public sealed class Gradient {
    internal abstract val start: Color
    internal abstract val end: Color

    internal class Vertical(override val start: Color, override val end: Color) : Gradient() {
        companion object {
            val DEFAULT: Vertical = Vertical(
                start = DEFAULT_START,
                end = DEFAULT_END,
            )
        }
    }

    internal class Horizontal(override val start: Color, override val end: Color) : Gradient() {
        companion object {
            val DEFAULT: Horizontal = Horizontal(
                start = DEFAULT_START,
                end = DEFAULT_END,
            )
        }
    }

    private companion object {

        val DEFAULT_START: Color = Color.Resource(R.color.audioburst_player_controller_gradient_default_start)

        val DEFAULT_END: Color = Color.Resource(R.color.audioburst_player_controller_gradient_default_end)
    }
}

private val Gradient.orientation: GradientDrawable.Orientation
    get() = when (this) {
        is Gradient.Horizontal -> GradientDrawable.Orientation.LEFT_RIGHT
        is Gradient.Vertical -> GradientDrawable.Orientation.TOP_BOTTOM
    }

public fun Gradient.toUniform(context: Context, cornerRadius: Float): GradientDrawable =
    toGradientDrawable(context, orientation, cornerRadius, arrayOf(start, end))

public fun Gradient.toBottomFocused(context: Context, cornerRadius: Float): GradientDrawable =
    toGradientDrawable(context, orientation, cornerRadius, arrayOf(start, start, end))

private fun toGradientDrawable(context: Context, orientation: GradientDrawable.Orientation, cornerRadius: Float, colorArray: Array<Color>): GradientDrawable {
    val colors = colorArray
        .map { it.colorInt(context) }
        .toIntArray()
    return GradientDrawable(orientation, colors).apply {
        setCornerRadius(cornerRadius)
    }
}