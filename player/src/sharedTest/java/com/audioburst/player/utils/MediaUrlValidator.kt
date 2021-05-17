package com.audioburst.player.utils

internal fun mediaUrlValidatorOf(isValid: Boolean = false): MediaUrlValidator = object : MediaUrlValidator {

    override fun isValid(url: String): Boolean = isValid
}