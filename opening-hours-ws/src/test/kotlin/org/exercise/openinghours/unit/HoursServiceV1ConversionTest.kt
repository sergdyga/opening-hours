package org.exercise.openinghours.unit

import org.assertj.core.api.Assertions.assertThat
import org.excercise.openinghours.HoursData
import org.excercise.openinghours.OpenClose.close
import org.excercise.openinghours.OpenClose.open
import org.exercise.openinghours.services.HoursServiceV1Impl
import org.exercise.openinghours.services.HoursServiceV2Impl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.DayOfWeek.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class HoursServiceV1ConversionTest {

    private val hoursServiceV1 = HoursServiceV1Impl(MONDAY)
    private val hoursServiceV2 = HoursServiceV2Impl(MONDAY)

    @Test
    fun `Converting from day to week format does not change output`() {
        val hours = mutableMapOf(
            SUNDAY to listOf(HoursData(open, 37800)),
            MONDAY to listOf(
                // Late closing hours
                HoursData(close, 1800),
                HoursData(open, 37800),
                HoursData(close, 64800)
            ),
            TUESDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
            THURSDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
            FRIDAY to listOf(
                // Close-open order mixed
                HoursData(close, 59400),
                HoursData(close, 37800),
                HoursData(close, 81000),
                HoursData(open, 45000),
                HoursData(open, 30000),
                HoursData(open, 70200),
            ),
            // Day of Week order mixed
            WEDNESDAY to listOf(),
            SATURDAY to listOf(HoursData(open, 0), HoursData(close, 86399)),
        )
        val responseFromDayFormat = hoursServiceV1.getFormattedHours(hours)
        assertThat(responseFromDayFormat).isEqualTo(listOf(
            "MONDAY: 10:30 am - 06:00 pm",
            "TUESDAY: 12:00 am - 11:59 pm",
            "WEDNESDAY: Closed",
            "THURSDAY: 12:00 am - 11:59 pm",
            "FRIDAY: 08:20 am - 10:30 am, 12:30 pm - 04:30 pm, 07:30 pm - 10:30 pm",
            "SATURDAY: 12:00 am - 11:59 pm",
            "SUNDAY: 10:30 am - 12:30 am"
        ))
        val converted = hoursServiceV1.convertToWeeklyHoursMap(hours)
        val responseFromWeekFormat = hoursServiceV2.getFormattedHours(converted)
        assertThat(responseFromDayFormat).isEqualTo(responseFromWeekFormat)
    }

}