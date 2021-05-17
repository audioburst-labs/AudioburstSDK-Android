package com.audioburst.player.models

import com.audioburst.library.models.*

internal fun burstOf(
    id: String = "",
    title: String = "",
    creationDate: String = "",
    duration: Duration = 0.0.toDuration(DurationUnit.Milliseconds),
    sourceName: String = "",
    category: String? = null,
    playlistId: Long = 0L,
    showName: String = "",
    streamUrl: String? = null,
    audioUrl: String = "",
    imageUrls: List<String> = emptyList(),
    source: BurstSource = burstSourceOf(),
    shareUrl: String = "",
    keywords: List<String> = emptyList(),
    ctaData: CtaData? = null,
    adUrl: String? = null,
): Burst =
    Burst(
        id = id,
        title = title,
        creationDate = creationDate,
        duration = duration,
        sourceName = sourceName,
        category = category,
        playlistId = playlistId,
        showName = showName,
        streamUrl = streamUrl,
        audioUrl = audioUrl,
        imageUrls = imageUrls,
        source = source,
        shareUrl = shareUrl,
        keywords = keywords,
        ctaData = ctaData,
        adUrl = adUrl,
    )

internal fun ctaDataOf(
    buttonText: String = "",
    url: String = "",
): CtaData =
    CtaData(
        buttonText = buttonText,
        url = url,
    )

internal fun burstSourceOf(
    sourceName: String = "",
    sourceType: String? = null,
    showName: String = "",
    durationFromStart: Duration = 0.0.toDuration(DurationUnit.Milliseconds),
    audioUrl: String? = null,
): BurstSource =
    BurstSource(
        sourceName = sourceName,
        sourceType = sourceType,
        showName = showName,
        durationFromStart = durationFromStart,
        audioUrl = audioUrl,
    )