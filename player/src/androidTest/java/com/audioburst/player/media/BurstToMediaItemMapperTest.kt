package com.audioburst.player.media

import com.audioburst.player.media.mappers.BurstToMediaItemMapper
import com.audioburst.player.models.burstOf
import com.audioburst.player.models.burstSourceOf
import com.google.android.exoplayer2.util.MimeTypes
import org.junit.Test

internal class BurstToMediaItemMapperTest {

    private val mapper = BurstToMediaItemMapper()

    private val mp3MimeType = MimeTypes.AUDIO_MPEG
    private val streamMimeType = MimeTypes.APPLICATION_M3U8

    @Test
    fun testIfBurstIdIsCorrectlyMapped() {
        // GIVEN
        val id = "id"
        val burst = burstOf(id = id)

        // WHEN
        val mapped = mapper.map(burst)

        // THEN
        assert(mapped.mediaId == id)
    }

    @Test
    fun testIfBurstIsMappedWithCorrectURLWhenStreamUrlIsNull() {
        // GIVEN
        val audioUrl = "https://storageaudiobursts.azureedge.net/audio/R0W2PMGO9P2V.mp3"
        val burst = burstOf(
            audioUrl = audioUrl,
            streamUrl = null,
        )

        // WHEN
        val mapped = mapper.map(burst)
        // THEN
        assert(mapped.playbackProperties?.uri?.toString() == audioUrl)
        assert(mapped.playbackProperties?.mimeType == mp3MimeType)
    }

    @Test
    fun testIfBurstIsMappedWithCorrectURLWhenStreamUrlIsNotNull() {
        // GIVEN
        val streamUrl = "https://storageaudiobursts.azureedge.net/stream/R0W2PMGO9P2V/outputlist.m3u8"
        val burst = burstOf(streamUrl = streamUrl)

        // WHEN
        val mapped = mapper.map(burst)
        // THEN
        assert(mapped.playbackProperties?.uri?.toString() == streamUrl)
        assert(mapped.playbackProperties?.mimeType == streamMimeType)
    }

    @Test
    fun testIfBurstIsMappedWithCorrectURLWhenAdIsAvailableForThisBurst() {
        // GIVEN
        val id = "id"
        val burst = burstOf(id = id, adUrl = "")

        // WHEN
        val mapped = mapper.map(burst)

        // THEN
        assert(mapped.playbackProperties?.uri?.toString()?.contains(id) == true)
        assert(mapped.playbackProperties?.mimeType == mp3MimeType)
    }

    @Test
    fun tesIifBurstIsMappedWithCorrectURLWhenMapWithSourceIsCalledAndSourcesAudioUrlIsNotNull() {
        // GIVEN
        val audioUrl = "https://storageaudiobursts.azureedge.net/audio/R0W2PMGO9P2V.mp3"
        val burst = burstOf(source = burstSourceOf(audioUrl = audioUrl))

        // WHEN
        val mapped = mapper.mapWithSource(burst)

        // THEN
        assert(mapped?.playbackProperties?.uri?.toString() == audioUrl)
        assert(mapped?.playbackProperties?.mimeType == mp3MimeType)
    }

    @Test
    fun tesIifBurstIsMappedWithCorrectURLWhenMapWithSourceIsCalledAndSourcesAudioUrlIsNull() {
        // GIVEN
        val burst = burstOf(source = burstSourceOf(audioUrl = null))

        // WHEN
        val mapped = mapper.mapWithSource(burst)

        // THEN
        assert(mapped == null)
    }
}