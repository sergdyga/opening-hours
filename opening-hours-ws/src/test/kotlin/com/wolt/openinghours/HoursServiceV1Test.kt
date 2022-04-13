package com.wolt.openinghours

import com.wolt.openinghours.OpenClose.close
import com.wolt.openinghours.OpenClose.open
import com.wolt.openinghours.services.HoursServiceV1Impl
import com.wolt.openinghours.utils.MissingOpenCloseHoursPairException
import com.wolt.openinghours.utils.WrongTimestampException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.DayOfWeek

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class HoursServiceV1Test {

    private val hoursService = HoursServiceV1Impl(DayOfWeek.MONDAY)

    private fun getHoursMap() = mutableMapOf(
        DayOfWeek.SUNDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        DayOfWeek.MONDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        DayOfWeek.TUESDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        DayOfWeek.WEDNESDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        DayOfWeek.THURSDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        DayOfWeek.FRIDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        DayOfWeek.SATURDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
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
        val response = hoursService.getFormattedHours(getHoursMap())
        assertThat(response).isEqualTo(allDaysOpenFormatted)
    }

    @Test
    fun `Empty days are returned as Closed`() {
        val response = hoursService.getFormattedHours(emptyMap())
        assertThat(response).isEqualTo(
            listOf(
                "MONDAY: Closed",
                "TUESDAY: Closed",
                "WEDNESDAY: Closed",
                "THURSDAY: Closed",
                "FRIDAY: Closed",
                "SATURDAY: Closed",
                "SUNDAY: Closed"
            )
        )
    }

    @Test
    fun `Multiple opening are allowed within the same day`() {
        val hours = getHoursMap()
        hours.replace(
            DayOfWeek.MONDAY, listOf(
                HoursData(open, 30000), HoursData(close, 37800),
                HoursData(open, 45000), HoursData(close, 59400),
                HoursData(open, 70200), HoursData(close, 81000),
            )
        )
        val response = hoursService.getFormattedHours(hours)
        assertThat(response[0]).isEqualTo("MONDAY: 08:20 am - 10:30 am, 12:30 pm - 04:30 pm, 07:30 pm - 10:30 pm")
    }

    @Test
    fun `Late closing hours are associated with previous day`() {
        val hours = getHoursMap()
        hours.replace(DayOfWeek.SUNDAY, listOf(HoursData(open, 37800)))
        hours.replace(DayOfWeek.MONDAY, listOf(HoursData(close, 1800), HoursData(open, 37800), HoursData(close, 64800)))
        val response = hoursService.getFormattedHours(hours)
        assertThat(response[6]).isEqualTo("SUNDAY: 10:30 am - 12:30 am")
        assertThat(response).isEqualTo(
            listOf(
                "MONDAY: 10:30 am - 06:00 pm",
                "TUESDAY: 12:00 am - 11:59 pm",
                "WEDNESDAY: 12:00 am - 11:59 pm",
                "THURSDAY: 12:00 am - 11:59 pm",
                "FRIDAY: 12:00 am - 11:59 pm",
                "SATURDAY: 12:00 am - 11:59 pm",
                "SUNDAY: 10:30 am - 12:30 am",
            )
        )
    }

    @Test
    fun `Hour is associated with previous day ONLY if open hour is in previous day`() {
        val hours = getHoursMap()
        hours.replace(
            DayOfWeek.MONDAY,
            listOf(
                HoursData(close, 1800), // This should NOT go to previous day, because previous open is in SAME day
                HoursData(close, 64800),
                HoursData(open, 37800),
                HoursData(open, 0),
            )
        )
        val response = hoursService.getFormattedHours(hours)
        assertThat(response).isEqualTo(
            listOf(
                "MONDAY: 12:00 am - 12:30 am, 10:30 am - 06:00 pm",
                "TUESDAY: 12:00 am - 11:59 pm",
                "WEDNESDAY: 12:00 am - 11:59 pm",
                "THURSDAY: 12:00 am - 11:59 pm",
                "FRIDAY: 12:00 am - 11:59 pm",
                "SATURDAY: 12:00 am - 11:59 pm",
                "SUNDAY: 12:00 am - 11:59 pm",
            )
        )
    }

    @Test
    fun `First day if week is configurable`() {
        val hours = getHoursMap()
        hours.replace(
            DayOfWeek.MONDAY, listOf(
                HoursData(close, 1800), HoursData(close, 64800),
                HoursData(open, 37800), HoursData(open, 0),
            )
        )
        var response = HoursServiceV1Impl(DayOfWeek.SUNDAY).getFormattedHours(hours)
        assertThat(response).isEqualTo(
            listOf(
                "SUNDAY: 12:00 am - 11:59 pm",
                "MONDAY: 12:00 am - 12:30 am, 10:30 am - 06:00 pm",
                "TUESDAY: 12:00 am - 11:59 pm",
                "WEDNESDAY: 12:00 am - 11:59 pm",
                "THURSDAY: 12:00 am - 11:59 pm",
                "FRIDAY: 12:00 am - 11:59 pm",
                "SATURDAY: 12:00 am - 11:59 pm",
            )
        )
        response = HoursServiceV1Impl(DayOfWeek.MONDAY).getFormattedHours(hours)
        assertThat(response).isEqualTo(
            listOf(
                "MONDAY: 12:00 am - 12:30 am, 10:30 am - 06:00 pm",
                "TUESDAY: 12:00 am - 11:59 pm",
                "WEDNESDAY: 12:00 am - 11:59 pm",
                "THURSDAY: 12:00 am - 11:59 pm",
                "FRIDAY: 12:00 am - 11:59 pm",
                "SATURDAY: 12:00 am - 11:59 pm",
                "SUNDAY: 12:00 am - 11:59 pm",
            )
        )
    }

    @Test
    fun `Hours are returned sorted`() {
        val hours = getHoursMap()
        hours.replace(DayOfWeek.TUESDAY, listOf(HoursData(close, 21600)))
        hours.replace(
            DayOfWeek.MONDAY, listOf(
                HoursData(open, 84000),
                HoursData(close, 64800),
                HoursData(open, 24000),
                HoursData(open, 37800),
                HoursData(close, 36000),
            )
        )
        val response = hoursService.getFormattedHours(hours)
        assertThat(response).isEqualTo(
            listOf(
                "MONDAY: 06:40 am - 10:00 am, 10:30 am - 06:00 pm, 11:20 pm - 06:00 am",
                "TUESDAY: Closed",
                "WEDNESDAY: 12:00 am - 11:59 pm",
                "THURSDAY: 12:00 am - 11:59 pm",
                "FRIDAY: 12:00 am - 11:59 pm",
                "SATURDAY: 12:00 am - 11:59 pm",
                "SUNDAY: 12:00 am - 11:59 pm"
            )
        )
    }

    @Test
    fun `Exception is thrown if open and close hours do not have pair`() {
        val hours = getHoursMap()
        hours.replace(DayOfWeek.MONDAY, listOf(HoursData(open, 37800)))
        assertThrows(MissingOpenCloseHoursPairException::class.java) { hoursService.getFormattedHours(hours) }
    }

    @Test
    fun `Exception is thrown if hours exceed limit`() {
        val hours = getHoursMap()
        hours.replace(DayOfWeek.MONDAY, listOf(HoursData(open, 0), HoursData(close, 86400)))
        assertThrows(WrongTimestampException::class.java) { hoursService.getFormattedHours(hours) }
        hours.replace(DayOfWeek.MONDAY, listOf(HoursData(open, -1), HoursData(close, 86399)))
        assertThrows(WrongTimestampException::class.java) { hoursService.getFormattedHours(hours) }
    }

}