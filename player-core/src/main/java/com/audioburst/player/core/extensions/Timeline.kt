package com.audioburst.player.core.extensions

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline

@Suppress("UNCHECKED_CAST")
internal fun Timeline.tags(shuffleModeEnabled: Boolean): List<String> {
    val tags = mutableListOf<String?>()
    val window = Timeline.Window()
    var windowIndex = getFirstWindowIndex(shuffleModeEnabled)
    while (windowIndex != C.INDEX_UNSET) {
        val tag = getWindow(windowIndex, window).mediaItem.mediaId
        tags.add(tag)
        windowIndex = getNextWindowIndex(windowIndex, Player.REPEAT_MODE_OFF, shuffleModeEnabled)
    }

    return tags.filterNotNull()
}
