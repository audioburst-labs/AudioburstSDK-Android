package com.audioburst.player.core.media

import android.support.v4.media.MediaBrowserCompat

internal class MediaSessionConnection(private val mediaBrowser: MediaBrowserCompat) {

    fun connect() {
        mediaBrowser.connect()
    }
}
