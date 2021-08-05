package com.audioburst.player.controller.utils

import android.content.Context
import android.content.res.Configuration.*
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.audioburst.player.controller.models.UiMode

public interface ResourceProvider {

    public fun getString(@StringRes id: Int): String

    public fun getString(@StringRes id: Int, vararg formatArgs: Any): String

    public fun getQuantityString(@PluralsRes id: Int, value: Int): String

    public fun getQuantityString(@PluralsRes id: Int, value: Int, vararg formatArgs: Any): String

    public fun getDrawable(@DrawableRes id: Int): Drawable?

    @ColorInt
    public fun getColor(@ColorRes id: Int): Int

    public fun getUiMode(): UiMode
}

internal class AndroidResourceProvider(private val context: Context) : ResourceProvider {

    override fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }

    override fun getString(@StringRes id: Int, vararg formatArgs: Any): String {
        return context.getString(id, *formatArgs)
    }

    override fun getQuantityString(@PluralsRes id: Int, value: Int): String {
        return context.resources.getQuantityString(id, value)
    }

    override fun getQuantityString(@PluralsRes id: Int, value: Int, vararg formatArgs: Any): String {
        return context.resources.getQuantityString(id, value, *formatArgs)
    }

    override fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }

    @ColorInt
    override fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    override fun getUiMode(): UiMode {
        return when (context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) {
            UI_MODE_NIGHT_NO -> UiMode.NightNo
            UI_MODE_NIGHT_YES -> UiMode.NightYes
            else -> UiMode.Undefined
        }
    }
}