package com.audioburst.player.utils

import com.audioburst.player.models.burstOf
import org.junit.Before
import org.junit.Test

internal class AdUrlCacheTest {

    private lateinit var cache: InMemoryAdUrlCache

    @Before
    fun initCache() {
        cache = InMemoryAdUrlCache()
    }

    @Test
    fun `test if adding value to cache works as expected`() {
        // GIVEN
        val burst = burstOf()
        val adUrl = "adUrl"

        // WHEN
        cache.set(burst, adUrl)

        // THEN
        assert(cache.get(burst) == adUrl)
    }

    @Test
    fun `test if clearing cache works as expected`() {
        // GIVEN
        val burst = burstOf()
        val adUrl = "adUrl"

        // WHEN
        cache.set(burst, adUrl)
        cache.clear()

        // THEN
        assert(cache.get(burst) == null)
    }
}

