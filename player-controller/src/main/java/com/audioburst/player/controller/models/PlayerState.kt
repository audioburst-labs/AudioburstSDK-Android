package com.audioburst.player.controller.models

import com.audioburst.library.models.ShareOptions

// TODO: Change to no data class
public data class PlayerState(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val isPlaying: Boolean = false,
    val isNextEnabled: Boolean = false,
    val isPreviousEnabled: Boolean = false,
    val ctaButtonText: String? = null,
    val switchSourceButton: SwitchSourceButton? = null,
    val shareOptions: ShareOptions? = null,
    val isLoadingShareUrl: Boolean = false,
    val isShareButton: Boolean = false,
    val playlistButtonState: PlaylistButtonState = PlaylistButtonState.Cards,
) {

    public enum class PlaylistButtonState {
        List, Cards
    }
}