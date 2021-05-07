package com.audioburst.player

import android.content.Context
import com.audioburst.player.di.Injector
import com.audioburst.player.media.BurstPlayer
import com.audioburst.player.media.MediaSessionConnection

public object Player {

    internal lateinit var mediaSessionConnection: MediaSessionConnection
    internal lateinit var burstPlayer: BurstPlayer

    public fun init(context: Context) {
        Injector.init(context)
        Injector.inject(this)
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSessionConnection.connect()
    }

    public fun getBurstPlayer(): BurstPlayer = burstPlayer
}