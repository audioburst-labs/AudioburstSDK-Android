package com.audioburst.player.controller

import com.audioburst.player.controller.models.*
import com.audioburst.player.controller.utils.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public interface PlayerController {

    public val playbackItemPosition: StateFlow<Int>

    public val scrollListToTop: Flow<Unit>

    public val cardItems: StateFlow<List<PlaylistCardItem>>

    public val listItems: StateFlow<List<PlaylistItem>>

    public val playerState: StateFlow<PlayerState>

    public val playerProgressBar: StateFlow<PlayerProgressBar>

    public val isSwipeTutorialVisible: Flow<Boolean>

    public val collectionState: StateFlow<PlayerCollectionState>

    public val adOverlay: StateFlow<AdOverlayState>

    public val playerStyle: StateFlow<PlayerStyle>

    public val resourceProvider: ResourceProvider

    public fun onEvent(uiEvent: UiEvent)
}