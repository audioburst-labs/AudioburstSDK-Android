package com.audioburst.player.controller

import android.content.Context
import com.audioburst.player.core.AudioburstPlayerCore

public object AudioburstPlayerController {

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
    }
}