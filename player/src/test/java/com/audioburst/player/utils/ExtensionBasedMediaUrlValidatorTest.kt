package com.audioburst.player.utils

import org.junit.Before
import org.junit.Test

internal class ExtensionBasedMediaUrlValidatorTest {

    private lateinit var validator: ExtensionBasedMediaUrlValidator

    @Before
    fun initValidator() {
        validator = ExtensionBasedMediaUrlValidator()
    }

    private fun testUrl(url: String, isValid: Boolean, ) {
        // WHEN
        val isUrlValid = validator.isValid(url)

        // THEN
        assert(isUrlValid == isValid)
    }

    @Test
    fun `test if mp3 url is valid`() {
        testUrl(
            url = "https://storageaudiobursts.azureedge.net/audio/R0W2PMGO9P2V.mp3",
            isValid = true
        )
    }

    @Test
    fun `test if m3u8 url is valid`() {
        testUrl(
            url = "https://storageaudiobursts.azureedge.net/stream/R0W2PMGO9P2V/outputlist.m3u8",
            isValid = true
        )
    }

    @Test
    fun `test if ts url is valid`() {
        testUrl(
            url = "https://storageaudiobursts.azureedge.net/outputlist.ts",
            isValid = true
        )
    }

    @Test
    fun `test if non media url is valid`() {
        testUrl(
            url = "https://storageaudiobursts.azureedge.net",
            isValid = false
        )
    }
}