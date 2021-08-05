package com.audioburst.player.controller.utils

internal interface ListenedBurstIds {

    fun clear()

    fun add(burstId: String)

    fun contains(burstId: String): Boolean
}

internal class InMemoryListenedBurstIdsHolder : ListenedBurstIds {

    private val listenedBurstIds = mutableListOf<String>()

    override fun clear() {
        listenedBurstIds.clear()
    }

    override fun add(burstId: String) {
        listenedBurstIds.add(burstId)
    }

    override fun contains(burstId: String): Boolean = listenedBurstIds.contains(burstId)
}