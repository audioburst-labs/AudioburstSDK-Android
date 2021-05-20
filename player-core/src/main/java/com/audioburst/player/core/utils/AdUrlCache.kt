package com.audioburst.player.core.utils

import com.audioburst.library.models.Burst

internal interface AdUrlCache {

    fun get(burst: Burst): String?

    fun set(burst: Burst, adUrl: String)

    fun clear()
}

internal class InMemoryAdUrlCache : AdUrlCache {

    private val map = mutableMapOf<Burst, String>()

    override fun get(burst: Burst): String? = map[burst]

    override fun set(burst: Burst, adUrl: String) {
        map[burst] = adUrl
    }

    override fun clear() {
        map.clear()
    }
}