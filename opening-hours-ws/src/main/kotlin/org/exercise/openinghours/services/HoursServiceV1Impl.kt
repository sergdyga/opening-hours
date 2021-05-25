package org.exercise.openinghours.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.excercise.openinghours.HoursData
import org.excercise.openinghours.OpenClose.close
import org.excercise.openinghours.OpenClose.open
import org.excercise.openinghours.Times
import org.exercise.openinghours.services.ServiceHelper.adjustFirstDayOfWeek
import org.exercise.openinghours.services.ServiceHelper.stringifyHours
import org.exercise.openinghours.utils.Constants.FULL_DAY
import org.exercise.openinghours.utils.Constants.FULL_WEEK
import org.exercise.openinghours.utils.Constants.OPEN_CLOSE_DELIMITER
import org.exercise.openinghours.utils.Constants.TIMES_DELIMITER
import org.exercise.openinghours.utils.MissingOpenCloseHoursPairException
import org.exercise.openinghours.utils.WrongTimestampException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import javax.annotation.PostConstruct

@Component
class HoursServiceV1Impl(@Value("\${settings.first-day-of-week}") private val firstDayOfWeek: DayOfWeek) : HoursServiceV1 {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val objectMapper = ObjectMapper()

    @PostConstruct
    fun log() {
        logger.info("HoursService instantiated with firstDayOfWeek: $firstDayOfWeek")
        require(firstDayOfWeek in arrayOf(MONDAY, SUNDAY)) { "Illegal first day of week $firstDayOfWeek" }
    }

    override fun getFormattedHours(hoursMap: Map<DayOfWeek, List<HoursData>>): List<String> {
        logger.debug("hoursMapJson: " + objectMapper.writeValueAsString(hoursMap))
        val mutableHoursMap = hoursMap.mapValues { it.value.toMutableList() }.toMutableMap()
        val formattedHoursList = formatToHumanReadable(mutableHoursMap)
            .entries.map { "${it.key}: ${it.value}" }
        return adjustFirstDayOfWeek(formattedHoursList, firstDayOfWeek)
    }

    override fun convertToWeeklyHoursMap(hoursMap: Map<DayOfWeek, List<HoursData>>): List<Times> {
        val flatHours = mutableListOf<HoursData>()

        hoursMap.entries.forEach { (day, hoursData) ->
            hoursData.forEach {
                val secondsFromStartOfWeek = FULL_DAY * day.ordinal
                flatHours.add(HoursData(it.type, it.value + secondsFromStartOfWeek))
            }
        }

        flatHours.sortBy { it.value }
        if (flatHours[0].type == close) {
            // Move late SUNDAY to next week
            val lateSundayHours = flatHours.removeAt(0)
            flatHours.add(HoursData(lateSundayHours.type, lateSundayHours.value + FULL_WEEK))
        }

        return flatHours.chunked(2).map { Times(it[0].value, it[1].value) }
    }

    private fun formatToHumanReadable(hoursMap: MutableMap<DayOfWeek, MutableList<HoursData>>): MutableMap<DayOfWeek, String> {
        // Sort and rearrange late hours
        hoursMap.values
            .forEach { dayHours ->
                dayHours.forEach { assertDayTimeLimits(it.value) }
                dayHours.sortBy { it.value } // Ensure times are sorted for each day
            }
        hoursMap.entries
            .forEach { (day, dayHours) ->
                if (dayHours.isNotEmpty() && dayHours.last().type != close) {
                    // If open without closed
                    // Append first time from next day
                    val firstClosingTime = hoursMap[day.plus(1)]!!.removeAt(0)
                    dayHours.add(firstClosingTime)
                }
            }

        val hoursFormatted = mutableMapOf<DayOfWeek, String>()
        DayOfWeek.values().associateTo(hoursFormatted) { it to "Closed" }

        // Finally stringify
        hoursMap.forEach { (day, hoursList) ->
            // Hours are paired and sorted so just concat
            if (hoursList.isNotEmpty()) {

                val string = hoursList
                    .asSequence()
                    .chunked(2) // Split to pairs of open-close
                    .map {
                        if (it[0].type != open) throw MissingOpenCloseHoursPairException("Missing open time for $day ? - ${it[0].value}")
                        if (it[1].type != close) throw MissingOpenCloseHoursPairException("Missing close time for $day ${it[0].value} - ?")

                        "${it[0].value.stringifyHours()}$OPEN_CLOSE_DELIMITER${it[1].value.stringifyHours()}"
                    }
                    .joinToString(TIMES_DELIMITER)
                hoursFormatted[day] = string
            }
        }

        return hoursFormatted
    }

    /**
     * Assertions
     */

    private fun assertDayTimeLimits(time: Long) {
        val maxValue = FULL_DAY
        if (time < 0) throw WrongTimestampException("Day opening hours must be positive")
        if (time >= maxValue) throw WrongTimestampException("Max value for day opening hours is $maxValue")
    }

}