package com.audioburst.player.core.media

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.audioburst.player.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

internal class NotificationBuilder private constructor(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(
            context, NOW_PLAYING_NOTIFICATION, NOW_PLAYING_CHANNEL, DescriptionAdapter(mediaController),
        )
            .setChannelNameResourceId(R.string.audioburst_player_core_notification_channel)
            .setChannelDescriptionResourceId(R.string.audioburst_player_core_notification_channel_description)
            .setNotificationListener(notificationListener)
            .build().apply {
                setMediaSessionToken(sessionToken)
                setSmallIcon(R.drawable.audioburst_player_core_ic_notification)
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUseNextActionInCompactView(true)
                setUsePreviousActionInCompactView(true)
            }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player) {
        notificationManager.setPlayer(player)
    }

    fun invalidate() {
        notificationManager.invalidate()
    }

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentTitle(player: Player): String =
            controller.metadata.description.title.toString()

        override fun getCurrentContentText(player: Player): String =
            controller.metadata.description.subtitle.toString()

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? = null
    }

    class Factory(private val context: Context) {
        fun create(
            sessionToken: MediaSessionCompat.Token,
            notificationListener: PlayerNotificationManager.NotificationListener,
        ): NotificationBuilder = NotificationBuilder(
            context = context,
            sessionToken = sessionToken,
            notificationListener = notificationListener,
        )
    }

    companion object {
        private const val NOW_PLAYING_CHANNEL: String = "com.audioburst.player.core.NOW_PLAYING"
        private const val NOW_PLAYING_NOTIFICATION: Int = 0xb339
    }
}