package com.audioburst.player.controller

import android.content.Context
import com.audioburst.library.models.SdkLevel
import com.audioburst.player.BuildConfig
import com.audioburst.player.controller.di.Injector
import com.audioburst.player.core.AudioburstPlayerCore

public object AudioburstPlayerController {

    private const val INIT_ERROR_MESSAGE = "Library is not initialized. You should call AudioburstPlayerController.init first."

    internal var _playerController: PlayerController? = null
    /**
     * Obtain this object to better control playback, observe its state and much more.
     */
    @JvmStatic
    public val playerController: PlayerController
        get() = if (_playerController == null) {
            error(INIT_ERROR_MESSAGE)
        } else {
            _playerController!!
        }

    /**
     * The function that initializes library. It should be used in the entry point of your application
     * (class that extends [Application]).
     *
     * @param context Context of the application.
     * @param applicationKey Key obtained from Audioburst Publishers (https://publishers.audioburst.com/).
     */
    @JvmStatic
    public fun init(context: Context, applicationKey: String) {
        AudioburstPlayerCore.init(context, applicationKey)
        AudioburstPlayerCore.audioburstLibrary.setSdkInfo(level = SdkLevel.Controller, version = BuildConfig.LIBRARY_VERSION)
        Injector.init(context)
        Injector.inject(this)
    }
}