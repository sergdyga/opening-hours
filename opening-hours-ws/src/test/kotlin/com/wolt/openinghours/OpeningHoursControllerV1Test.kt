package com.wolt.openinghours

import com.wolt.openinghours.controllers.OpeningHoursControllerV1
import com.wolt.openinghours.services.HoursServiceV1Impl
import com.wolt.openinghours.utils.WrongDayOfWeekException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpeningHoursControllerV1Test {

    private val hoursService = mock(HoursServiceV1Impl::class.java)
    private val controllerV1 = OpeningHoursControllerV1(hoursService)

    @BeforeAll
    fun initMocks() {
        `when`(hoursService.getFormattedHours(anyMap())).thenReturn(listOf("1", "2", "3"))
    }

    @Test
    fun `Days are parsed as enum from string case insensitive`() {
        val hours = mutableMapOf(
            "sunday" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "MONDAY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "TuesdaY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "WEDNESDAY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "THURSDAY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "FRIDAY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "SATURDAY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
        )
        controllerV1.formatHours(hours)
    }

    @Test
    fun `Returned list is joined with new line separator`() {
        val hours = mutableMapOf(
            "sunday" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "MONDAY" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
        )
        `when`(hoursService.getFormattedHours(anyMap())).thenReturn(listOf("1", "2", "3"))
        assertThat(controllerV1.formatHours(hours)).isEqualTo("1\n2\n3")
    }

    @Test
    fun `Exception is thrown if wrong day name`() {
        val hours = mutableMapOf(
            "sunday" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
            "MONDAY1" to listOf(HoursData(OpenClose.open, 0), HoursData(OpenClose.close, 86399)),
        )
        Assertions.assertThrows(WrongDayOfWeekException::class.java) { controllerV1.formatHours(hours) }
    }

}