package com.audioburst.player.di

import com.audioburst.player.di.provider.Provider
import com.audioburst.player.di.provider.provider
import com.audioburst.player.di.provider.singleton
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ProviderTest {

    @Test
    fun `test if new instance is created everytime we call provider function`() {
        // GIVEN
        val provider: Provider<Mock> = provider { Mock() }

        // WHEN
        val mocks = Array(100) { provider.get() }

        // THEN
        assertEquals(mocks.toSet().size, mocks.size)
    }

    @Test
    fun `test if single instance is returned everytime we call provider function`() {
        // GIVEN
        val provider: Provider<Mock> = singleton { Mock() }

        // WHEN
        val mocks = Array(100) { provider.get() }

        // THEN
        assertEquals(mocks.toSet().size, 1)
    }
}

private class Mock