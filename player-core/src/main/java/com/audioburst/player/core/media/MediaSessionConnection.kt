package com.audioburst.player.core.media

import android.support.v4.media.MediaBrowserCompat

internal class MediaSessionConnection(private val mediaBrowser: MediaBrowserCompat) {

    val isConnected: Boolean
        get() = mediaBrowser.isConnected

    fun connect() {
        if (!mediaBrowser.isConnected) {
            mediaBrowser.connect()
        }
    }

    fun disconnect() {
        if (mediaBrowser.isConnected) {
            mediaBrowser.disconnect()
        }
    }
}
