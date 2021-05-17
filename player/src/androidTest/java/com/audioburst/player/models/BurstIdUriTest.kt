package com.audioburst.player.models

import android.net.Uri
import org.junit.Test

internal class BurstIdUriTest {

    @Test
    fun testBurstWithIdAuthorityIsEqualToBurstId() {
        // GIVEN
        val burstId = "burstId"
        val burst = burstOf(id = burstId)

        // WHEN
        val burstIdUri = BurstIdUri(burst)

        // THEN
        assert(burstIdUri.uri.authority == burstId)
    }

    @Test
    fun testIfBurstWithIdIsCreatedProperly() {
        // GIVEN
        val burstId = "burstId"
        val expectedUriString = "burstId://$burstId"
        val burst = burstOf(id = burstId)

        // WHEN
        val burstIdUri = BurstIdUri(burst)

        // THEN
        assert(burstIdUri.uri.toString() == expectedUriString)
    }

    @Test
    fun testIfBurstIdIsGettingReturnedWhenUriHasCorrectSchema() {
        // GIVEN
        val burstId = "id"
        val uriString = "burstId://$burstId"
        val uri = Uri.parse(uriString)

        // WHEN
        val id = BurstIdUri.burstIdFrom(uri)

        // THEN
        assert(id == burstId)
    }

    @Test
    fun testIfNullIdIsGettingReturnedWhenUriDoesntHaveCorrectSchema() {
        // GIVEN
        val burstId = "id"
        val uriString = "burst://$burstId"
        val uri = Uri.parse(uriString)

        // WHEN
        val id = BurstIdUri.burstIdFrom(uri)

        // THEN
        assert(id == null)
    }
}