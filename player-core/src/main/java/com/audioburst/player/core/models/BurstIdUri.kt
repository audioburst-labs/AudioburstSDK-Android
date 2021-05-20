package com.audioburst.player.core.models

import android.net.Uri
import com.audioburst.library.models.Burst

internal class BurstIdUri(private val burst: Burst) {

    val uri: Uri
        get() = Uri.Builder()
            .scheme(BURST_ID_SCHEME)
            .authority(burst.id)
            .build()

    companion object {
        private const val BURST_ID_SCHEME = "burstId"

        fun burstIdFrom(uri: Uri): String? =
            if (uri.scheme == BURST_ID_SCHEME) {
                uri.authority
            } else {
                null
            }
    }
}