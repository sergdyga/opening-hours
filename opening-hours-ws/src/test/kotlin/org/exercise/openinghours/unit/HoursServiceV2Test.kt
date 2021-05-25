package org.exercise.openinghours.unit

import org.assertj.core.api.Assertions.assertThat
import org.excercise.openinghours.CloseHourBeforeOpenHourException
import org.excercise.openinghours.Times
import org.exercise.openinghours.services.HoursServiceV2Impl
import org.exercise.openinghours.utils.HoursOverlapException
import org.exercise.openinghours.utils.WrongTimestampException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.DayOfWeek

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class HoursServiceV2Test {

    private val hoursService = HoursServiceV2Impl(DayOfWeek.MONDAY)

    private fun getHoursList() = listOf(
        Times(0, 86399),
        Times(86400, 172799),
        Times(172800, 259199),
        Times(259200, 345599),
        Times(345600, 431999),
        Times(432000, 518399),
        Times(518400, 604799),
    )

    private val allDaysOpenFormatted = listOf(
        "MONDAY: 12:00 am - 11:59 pm",
        "TUESDAY: 12:00 am - 11:59 pm",
        "WEDNESDAY: 12:00 am - 11:59 pm",
        "THURSDAY: 12:00 am - 11:59 pm",
        "FRIDAY: 12:00 am - 11:59 pm",
        "SATURDAY: 12:00 am - 11:59 pm",
        "SUNDAY: 12:00 am - 11:59 pm"
    )

    @Test
    fun `All days are returned in order`() {
        val response = hoursService.getFormattedHours(getHoursList().shuffled())
        assertThat(response).isEqualTo(allDaysOpenFormatted)
    }

    @Test
    fun `Empty days are returned as Closed`() {
        val response = hoursService.getFormattedHours(emptyList())
        assertThat(response).isEqualTo(listOf(
            "MONDAY: Closed",
            "TUESDAY: Closed",
            "WEDNESDAY: Closed",
            "THURSDAY: Closed",
            "FRIDAY: Closed",
            "SATURDAY: Closed",
            "SUNDAY: Closed"
        ))
    }

    @Test
    fun `Multiple opening are allowed within the same day`() {
        val hours = listOf(
            Times(518400, 604799),
            Times(30000, 37800),
            Times(45000, 59400),
            Times(70200, 81000),
            Times(86400, 172799),
            Times(172800, 259199),
            Times(259200, 345599),
            Times(345600, 431999),
            Times(432000, 518399)
        )
        val response = hoursService.getFormattedHours(hours)
        println(response.joinToString("\n"))
        assertThat(response[0]).isEqualTo("MONDAY: 08:20 am - 10:30 am, 12:30 pm - 04:30 pm, 07:30 pm - 10:30 pm")
    }

    @Test
    fun `Late closing hours are associated with previous day`() {
        val hours = listOf(
            Times(37800, 64800),
            Times(86400, 172799),
            Times(172800, 259199),
            Times(259200, 345599),
            Times(345600, 431999),
            Times(432000, 518399),
            Times(563400, 610200) // This is SUNDAY
        )
        val response = hoursService.getFormattedHours(hours)
        assertThat(response[6]).isEqualTo("SUNDAY: 12:30 pm - 01:30 am")
    }

    @Test
    fun `Hour is associated with previous day ONLY if open hour is in previous day`() {
        val hours = listOf(
            Times(0, 1800), // This should be MONDAY, not late SUNDAY
            Times(37800, 64800),
            Times(86400, 172799),
            Times(172800, 259199),
            Times(259200, 345599),
            Times(345600, 431999),
            Times(432000, 518399),
            Times(556220, 604799),
        )
        val response = hoursService.getFormattedHours(hours)
        assertThat(response).isEqualTo(listOf(
            "MONDAY: 12:00 am - 12:30 am, 10:30 am - 06:00 pm",
            "TUESDAY: 12:00 am - 11:59 pm",
            "WEDNESDAY: 12:00 am - 11:59 pm",
            "THURSDAY: 12:00 am - 11:59 pm",
            "FRIDAY: 12:00 am - 11:59 pm",
            "SATURDAY: 12:00 am - 11:59 pm",
            "SUNDAY: 10:30 am - 11:59 pm",
        ))
    }

    @Test
    fun `Hours are returned sorted`() {
        val hours = listOf(
            Times(30000, 37800),
            Times(45000, 59400),
            Times(70200, 81000),
            Times(172800, 259199),
            Times(259200, 345599),
            Times(345600, 431999),
            Times(432000, 518399),
            Times(518400, 604799),
        ).shuffled()
        val response = hoursService.getFormattedHours(hours)
        assertThat(response[0]).isEqualTo("MONDAY: 08:20 am - 10:30 am, 12:30 pm - 04:30 pm, 07:30 pm - 10:30 pm")
        assertThat(response).isEqualTo(listOf(
            "MONDAY: 08:20 am - 10:30 am, 12:30 pm - 04:30 pm, 07:30 pm - 10:30 pm",
            "TUESDAY: Closed",
            "WEDNESDAY: 12:00 am - 11:59 pm",
            "THURSDAY: 12:00 am - 11:59 pm",
            "FRIDAY: 12:00 am - 11:59 pm",
            "SATURDAY: 12:00 am - 11:59 pm",
            "SUNDAY: 12:00 am - 11:59 pm"
        ))
    }

    @Test
    fun `First day if week is configurable`() {
        val hours = getHoursList()
        var response = HoursServiceV2Impl(DayOfWeek.MONDAY).getFormattedHours(hours)
        assertThat(response).isEqualTo(allDaysOpenFormatted)
        response = HoursServiceV2Impl(DayOfWeek.SUNDAY).getFormattedHours(hours)
        assertThat(response).isEqualTo(listOf(
            "SUNDAY: 12:00 am - 11:59 pm",
            "MONDAY: 12:00 am - 11:59 pm",
            "TUESDAY: 12:00 am - 11:59 pm",
            "WEDNESDAY: 12:00 am - 11:59 pm",
            "THURSDAY: 12:00 am - 11:59 pm",
            "FRIDAY: 12:00 am - 11:59 pm",
            "SATURDAY: 12:00 am - 11:59 pm",
        ))
    }

    @Test
    fun `Exception is thrown if open hour is after close hour`() {
        assertThrows(CloseHourBeforeOpenHourException::class.java) { listOf(Times(604800, 518400)) }
        assertThrows(CloseHourBeforeOpenHourException::class.java) { listOf(Times(518400, 518400)) }
    }

    @Test
    fun `Exception is thrown if hours overlap`() {
        val closeOpenOverlap = listOf(Times(0, 3600), Times(3599, 64000))
        assertThrows(HoursOverlapException::class.java) { hoursService.getFormattedHours(closeOpenOverlap) }
        val sameValues = listOf(Times(0, 3600), Times(3600, 64000))
        assertThrows(HoursOverlapException::class.java) { hoursService.getFormattedHours(sameValues) }
        val weekEndOverlap = listOf(
            Times(0, 1800), // Opens on MONDAY 12:00 am
            Times(556220, 604800), // Closed on MONDAY 12:00 am
        )
        assertThrows(HoursOverlapException::class.java) { hoursService.getFormattedHours(weekEndOverlap) }
    }

    @Test
    fun `Exception is thrown if hours exceed limit`() {
        val hoursAboveMax = listOf(Times(518400, 691200))
        assertThrows(WrongTimestampException::class.java) { hoursService.getFormattedHours(hoursAboveMax) }
        val hoursBelowMin = listOf(Times(-1, 86399))
        assertThrows(WrongTimestampException::class.java) { hoursService.getFormattedHours(hoursBelowMin) }
    }

}