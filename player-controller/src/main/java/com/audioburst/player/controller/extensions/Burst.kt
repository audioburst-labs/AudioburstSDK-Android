package com.audioburst.player.controller.extensions

import com.audioburst.library.models.Burst
import com.audioburst.player.controller.R
import com.audioburst.player.controller.models.Color
import com.audioburst.player.controller.models.Gradient
import com.audioburst.player.controller.models.TimeUnit
import com.audioburst.player.controller.utils.ResourceProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

private fun String.toZonedDateTime(): ZonedDateTime? =
    if (isEmpty()) {
        null
    } else {
        ZonedDateTime.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }?.withZoneSameInstant(ZoneId.systemDefault())

internal fun Burst.airedText(resourceProvider: ResourceProvider): String {
    val unavailable = resourceProvider.getString(R.string.audioburst_player_controller_date_unavailable)
    val date = creationDate.toZonedDateTime() ?: return unavailable
    return if (date.isAfter(ZonedDateTime.now())) {
        resourceProvider.getString(R.string.audioburst_player_controller_burst_aired_time_now)
    } else {
        relativeDateText(resourceProvider, date)
    }
}

private fun relativeDateText(resourceProvider: ResourceProvider, date: ZonedDateTime): String {
    val now = ZonedDateTime.now()
    val timeUnits = listOf(
        TimeUnit.Minute(startTime = date, endTime = now),
        TimeUnit.Hour(startTime = date, endTime = now),
        TimeUnit.Day(startTime = date, endTime = now),
        TimeUnit.Month(startTime = date, endTime = now),
        TimeUnit.Year(startTime = date, endTime = now),
    )
    return timeUnits.lastOrNull { it.value > 0 }?.agoText(resourceProvider) ?: timeUnits.first().agoText(resourceProvider)
}

internal val Burst.verticalGradient: Gradient
    get()  {
        val gradients = listOf(
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_first_top),
                end = Color.Resource(R.color.audioburst_player_controller_first_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_second_top),
                end = Color.Resource(R.color.audioburst_player_controller_second_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_third_top),
                end = Color.Resource(R.color.audioburst_player_controller_third_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_fourth_top),
                end = Color.Resource(R.color.audioburst_player_controller_fourth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_fifth_top),
                end = Color.Resource(R.color.audioburst_player_controller_fifth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_sixth_top),
                end = Color.Resource(R.color.audioburst_player_controller_sixth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_seventh_top),
                end = Color.Resource(R.color.audioburst_player_controller_seventh_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_eighth_top),
                end = Color.Resource(R.color.audioburst_player_controller_eighth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_ninth_top),
                end = Color.Resource(R.color.audioburst_player_controller_ninth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_tenth_top),
                end = Color.Resource(R.color.audioburst_player_controller_tenth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_eleventh_top),
                end = Color.Resource(R.color.audioburst_player_controller_eleventh_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_twelfth_top),
                end = Color.Resource(R.color.audioburst_player_controller_twelfth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_thirteenth_top),
                end = Color.Resource(R.color.audioburst_player_controller_thirteenth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_fourteenth_top),
                end = Color.Resource(R.color.audioburst_player_controller_fourteenth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_fifteenth_top),
                end = Color.Resource(R.color.audioburst_player_controller_fifteenth_bottom)
            ),
            Gradient.Vertical(
                start = Color.Resource(R.color.audioburst_player_controller_sixteenth_top),
                end = Color.Resource(R.color.audioburst_player_controller_sixteenth_bottom)
            )
        )
        return gradients[id.hashCode().absoluteValue.rem(gradients.size)]
    }