package org.exercise.openinghours.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.excercise.openinghours.Times
import org.exercise.openinghours.services.ServiceHelper.adjustFirstDayOfWeek
import org.exercise.openinghours.services.ServiceHelper.stringifyHours
import org.exercise.openinghours.services.ServiceHelper.toDayOfWeek
import org.exercise.openinghours.utils.Constants.FULL_WEEK
import org.exercise.openinghours.utils.Constants.FULL_WEEK_PLUS_DAY
import org.exercise.openinghours.utils.Constants.OPEN_CLOSE_DELIMITER
import org.exercise.openinghours.utils.Constants.TIMES_DELIMITER
import org.exercise.openinghours.utils.HoursOverlapException
import org.exercise.openinghours.utils.WrongTimestampException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import javax.annotation.PostConstruct

/**
 * POC of weekly input format
 */
@Component
class HoursServiceV2Impl(@Value("\${settings.first-day-of-week}") private val firstDayOfWeek: DayOfWeek) :
    HoursServiceV2 {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val objectMapper = ObjectMapper()

    @PostConstruct
    fun log() {
        logger.info("HoursService instantiated with firstDayOfWeek: $firstDayOfWeek")
        require(firstDayOfWeek in arrayOf(MONDAY, SUNDAY)) { "Illegal first day of week $firstDayOfWeek" }
    }

    override fun getFormattedHours(hoursList: List<Times>): List<String> {
        logger.debug("hoursListJson: " + objectMapper.writeValueAsString(hoursList))
        val formattedHoursList = formatToHumanReadable(hoursList.toMutableList())
            .entries.map { "${it.key}: ${it.value}" }
        return adjustFirstDayOfWeek(formattedHoursList, firstDayOfWeek)
    }

    private fun formatToHumanReadable(hoursList: MutableList<Times>): LinkedHashMap<DayOfWeek, StringBuilder> {
        // Sort and rearrange late hours
        hoursList.sortBy { it.from }

        // Assertions
        validateInputData(hoursList)

        val hoursFormatted = LinkedHashMap<DayOfWeek, StringBuilder>()
        DayOfWeek.values().associateTo(hoursFormatted) { it to StringBuilder("Closed") }

        // Now stringify
        hoursList.forEach { (from, to) ->
            val day = from.toDayOfWeek()
            val string = "${from.stringifyHours()}$OPEN_CLOSE_DELIMITER${to.stringifyHours()}"
            if (hoursFormatted[day].toString() == "Closed") {
                hoursFormatted[day] = StringBuilder(string)
            } else {
                hoursFormatted[day]!!.append(TIMES_DELIMITER).append(string)
            }
        }

        return hoursFormatted
    }

    /**
     * Validation implies that [hoursList] is sorted
     */
    private fun validateInputData(hoursList: List<Times>) {
        if (hoursList.isNotEmpty()) {
            val last = hoursList.last()
            val first = hoursList[0]
            hoursList.zipWithNext().forEach {
                if (it.second.from <= it.first.to) throw HoursOverlapException("Opening hours overlap: ${it.first} and ${it.second}")
            }
            if (last.to - FULL_WEEK >= first.from) {
                throw HoursOverlapException("Closing hours overlap: $last overlap opening hours of $first")
            }
            val maxValue = FULL_WEEK_PLUS_DAY
            if (first.from < 0) throw WrongTimestampException("Opening hours must be positive")
            if (last.to >= maxValue) throw WrongTimestampException("Max value for week opening hours is $maxValue")
        }
    }

}