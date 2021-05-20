package com.audioburst.player.core.extensions

import android.support.v4.media.session.PlaybackStateCompat

internal inline val PlaybackStateCompat.isPrepared
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING) ||
            (state == PlaybackStateCompat.STATE_PAUSED)

internal inline val PlaybackStateCompat.isPlayEnabled
    get() = (actions and PlaybackStateCompat.ACTION_PLAY != 0L) ||
            ((actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                    (state == PlaybackStateCompat.STATE_PAUSED))

internal inline val PlaybackStateCompat.isPauseEnabled
    get() = (actions and PlaybackStateCompat.ACTION_PAUSE != 0L) ||
            ((actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                    (state == PlaybackStateCompat.STATE_BUFFERING ||
                            state == PlaybackStateCompat.STATE_PLAYING))

internal inline val PlaybackStateCompat.isSkipToNextEnabled
    get() = actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L

internal inline val PlaybackStateCompat.isSkipToPreviousEnabled
    get() = actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L