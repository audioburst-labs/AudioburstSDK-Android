package com.audioburst.player.controller.utils;

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri

internal interface UrlOpener {

    fun open(url: String): Boolean

    fun share(text: String, subject: String?): Boolean
}

internal class AndroidUrlOpener(private val context: Context) : UrlOpener {

    override fun open(url: String): Boolean =
        context.open(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = FLAG_ACTIVITY_NEW_TASK
            }
        )

    override fun share(text: String, subject: String?): Boolean {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"

            putExtra(Intent.EXTRA_TEXT, text)
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }

        return context.open(Intent.createChooser(sendIntent, null).addFlags(FLAG_ACTIVITY_NEW_TASK))
    }

    private fun Context.open(intent: Intent): Boolean =
        try {
            startActivity(intent)
            true
        } catch (exception: ActivityNotFoundException) {
            false
        }
}