package org.exercise.openinghours.services

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ServiceHelper {

    fun Long.stringifyHours(): String {
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
        return Instant.ofEpochSecond(this)
            .atZone(ZoneId.of("UTC"))
            .toLocalDateTime()
            .plusDays(4) // Correcting 0 to Monday for week representation
            .format(timeFormat)
    }

    fun Long.toDayOfWeek(): DayOfWeek {
        return Instant.ofEpochSecond(this)
            .atZone(ZoneId.of("UTC"))
            .plusDays(4) // Correcting 0 to Monday for week representation
            .dayOfWeek
    }

    fun adjustFirstDayOfWeek(hoursList: List<String>, firstDayOfWeek: DayOfWeek): List<String> {
        return if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val mutableHoursList = hoursList.toMutableList()
            val sunday = mutableHoursList.removeAt(6)
            mutableHoursList.add(0, sunday)
            mutableHoursList
        } else hoursList
    }


}