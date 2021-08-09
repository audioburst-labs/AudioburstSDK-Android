package com.audioburst.player.controller.models

import com.audioburst.library.models.ShareData

public sealed class UiEvent {

    public object PlayPauseButtonClick : UiEvent()
    public object PreviousButtonClick : UiEvent()
    public object NextButtonClick : UiEvent()
    public object OnRewindButtonClick : UiEvent()
    public class OnPlaylistItemClick(internal val burstIndicator: BurstIndicator) : UiEvent()
    public class OnShareButtonClick(internal val burstIndicator: BurstIndicator) : UiEvent()
    public object OnShareOptionsClose : UiEvent()
    public class OnShareOptionsChosen(internal val shareData: ShareData) : UiEvent()
    public class OnLikeButtonClick(internal val burstIndicator: BurstIndicator) : UiEvent()
    public object OnSwitchSource : UiEvent()
    public class OnCtaButtonClick(internal val burstIndicator: BurstIndicator) : UiEvent()
    public object SkipAdButtonClick : UiEvent()
    public class OnSeekTo(internal val position: Int) : UiEvent()
    public object OnStartSeeking : UiEvent()

    public sealed class BurstIndicator {
        public class BurstId(internal val value: String) : BurstIndicator()
        public class Position(internal val value: Int) : BurstIndicator()
        public object CurrentlyPlaying : BurstIndicator()
    }
}