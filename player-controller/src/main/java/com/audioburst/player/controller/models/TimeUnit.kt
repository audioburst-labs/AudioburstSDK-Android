package com.audioburst.player.controller.models

import androidx.annotation.PluralsRes
import com.audioburst.player.controller.R
import com.audioburst.player.controller.utils.ResourceProvider
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal sealed class TimeUnit {

    @get:PluralsRes
    protected abstract val plural: Int
    protected abstract val chronoUnit: ChronoUnit

    protected abstract val startTime: ZonedDateTime
    protected abstract val endTime: ZonedDateTime

    val value: Int
        get() = chronoUnit.between(startTime, endTime).toInt()

    fun agoText(resourceProvider: ResourceProvider): String {
        return resourceProvider.getQuantityString(plural, value, value)
    }

    class Second(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_seconds_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.SECONDS
    }

    class Minute(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_minutes_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.MINUTES
    }

    class Hour(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_hours_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.HOURS
    }

    class Day(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_days_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.DAYS
    }

    class Week(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_weeks_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.WEEKS
    }

    class Month(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_months_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.MONTHS
    }

    class Year(
        override val startTime: ZonedDateTime,
        override val endTime: ZonedDateTime,
    ) : TimeUnit() {

        override val plural: Int
            get() = R.plurals.audioburst_player_controller_years_ago

        override val chronoUnit: ChronoUnit
            get() = ChronoUnit.YEARS
    }
}