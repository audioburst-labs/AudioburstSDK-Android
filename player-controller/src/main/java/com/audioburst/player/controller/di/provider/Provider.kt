package com.audioburst.player.controller.di.provider

internal interface Provider<T> {

    fun get(): T
}

internal fun <T> provider(creator: () -> T): Provider<T> =
    object : Provider<T> {
        override fun get(): T = creator()
    }

internal abstract class Singleton<T : Any>: Provider<T> {
    private var t: T? = null

    protected abstract fun creator(): T

    override fun get(): T =
        t ?: creator().apply {
            t = this
        }
}

internal fun <T : Any> singleton(creator: () -> T): Singleton<T> =
    object : Singleton<T>() {
        override fun creator(): T = creator()
    }