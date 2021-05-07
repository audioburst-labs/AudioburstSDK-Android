package com.audioburst.player.media

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

internal class MediaQueueNavigator(mediaSession: MediaSessionCompat) : TimelineQueueNavigator(mediaSession) {

    private val window = Timeline.Window()

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        val mediaItem = player.currentTimeline.getWindow(windowIndex, window).mediaItem
        return MediaDescriptionCompat.Builder()
            .setMediaId(mediaItem.mediaId)
            .setMediaUri(mediaItem.playbackProperties?.uri)
            .build()
    }
}