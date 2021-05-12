package com.audioburst.player.models

public sealed class MediaUrl {
    public abstract val url: String

    public class Burst(override val url: String) : MediaUrl()
    public class Source(override val url: String) : MediaUrl()
    public class Advertisement(override val url: String) : MediaUrl()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaUrl

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    private val className: String
        get() = when (this) {
            is Advertisement -> "Advertisement"
            is Burst -> "Burst"
            is Source -> "Source"
        }

    override fun toString(): String {
        return "$className(url=$url)"
    }
}