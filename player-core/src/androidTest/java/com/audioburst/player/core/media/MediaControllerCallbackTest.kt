package com.audioburst.player.core.media

import org.junit.Before
import org.junit.Test

internal class MediaControllerCallbackTest {

    private lateinit var callback: MediaControllerCallback

    @Before
    fun initCallback() {
        callback = MediaControllerCallback(MediaBrowserConnectionCallback())
    }

    @Test
    fun testIfInitialValuePlaybackStateIsEmpty() {
        assert(callback.playbackState.value == EMPTY_PLAYBACK_STATE)
    }

    @Test
    fun testIfPlaybackStateIsEmptyWhenPlaybackStateChangedIsNull() {
        // WHEN
        callback.onPlaybackStateChanged(null)

        // THEN
        assert(callback.playbackState.value == EMPTY_PLAYBACK_STATE)
    }

    @Test
    fun testIfPlaybackStateIsEmittedWhenPlaybackStateChangedToSomeValue() {
        // GIVEN
        val value = EMPTY_PLAYBACK_STATE

        // WHEN
        callback.onPlaybackStateChanged(value)

        // THEN
        assert(callback.playbackState.value == value)
    }

    @Test
    fun testIfInitialValueNowPlayingIsNothing() {
        // THEN
        assert(callback.nowPlaying.value == NOTHING_PLAYING)
    }

    @Test
    fun testIfNowPlayingIsNothingWhenMetadataChangedIsNull() {
        // WHEN
        callback.onMetadataChanged(null)

        // THEN
        assert(callback.nowPlaying.value == NOTHING_PLAYING)
    }

    @Test
    fun testIfNowPlayingIsEmittedWhenMetadataChangedToSomeValue() {
        // GIVEN
        val value = NOTHING_PLAYING

        // WHEN
        callback.onMetadataChanged(value)

        // THEN
        assert(callback.nowPlaying.value == value)
    }
}