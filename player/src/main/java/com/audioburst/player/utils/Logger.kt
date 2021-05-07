package com.audioburst.player.utils

import android.util.Log
import com.audioburst.player.BuildConfig

internal object Logger {

    private const val TAG = "AudioburstPlayerCore"

    fun logException(exception: Exception) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, exception.stackTraceToString())
        }
    }
}