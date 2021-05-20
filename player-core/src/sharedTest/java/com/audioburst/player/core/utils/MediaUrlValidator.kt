package com.audioburst.player.core.utils

internal fun mediaUrlValidatorOf(isValid: Boolean = false): MediaUrlValidator = object :
    MediaUrlValidator {

    override fun isValid(url: String): Boolean = isValid
}