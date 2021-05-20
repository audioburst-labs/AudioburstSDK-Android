package com.audioburst.player.core.media

import org.junit.Before
import org.junit.Test

internal class MediaBrowserConnectionCallbackTest {

    private lateinit var callback: MediaBrowserConnectionCallback

    @Before
    fun initCallback() {
        callback = MediaBrowserConnectionCallback()
    }

    @Test
    fun testIfInitialValueIsDisconnected() {
        assert(callback.state.value == MediaBrowserConnectionState.Disconnected)
    }

    @Test
    fun testIfOnConnectedIsCalledStateIsConnected() {
        // WHEN
        callback.onConnected()

        // THEN
        assert(callback.state.value == MediaBrowserConnectionState.Connected)
    }

    @Test
    fun testIfOnConnectionFailedIsCalledStateIsFailed() {
        // WHEN
        callback.onConnectionFailed()

        // THEN
        assert(callback.state.value == MediaBrowserConnectionState.Failed)
    }

    @Test
    fun testIfOnConnectionSuspendedIsCalledStateIsSuspended() {
        // WHEN
        callback.onConnectionSuspended()

        // THEN
        assert(callback.state.value == MediaBrowserConnectionState.Suspended)
    }
}