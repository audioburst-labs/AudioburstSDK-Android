package com.audioburst.player.core.extensions

import android.support.v4.media.MediaMetadataCompat

internal inline val MediaMetadataCompat.duration: Long
    get() = this.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)