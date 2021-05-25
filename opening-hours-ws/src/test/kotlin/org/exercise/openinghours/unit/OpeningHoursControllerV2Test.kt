package org.exercise.openinghours.unit

import org.assertj.core.api.Assertions.assertThat
import org.excercise.openinghours.Times
import org.exercise.openinghours.controllers.OpeningHoursControllerV2
import org.exercise.openinghours.services.HoursServiceV2Impl
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpeningHoursControllerV2Test {

    private val hoursService = mock(HoursServiceV2Impl::class.java)
    private val controllerV2 = OpeningHoursControllerV2(hoursService)

    @BeforeAll
    fun initMocks() {
        `when`(hoursService.getFormattedHours(anyList())).thenReturn(listOf("1", "2", "3"))
    }

    @Test
    fun `Returned list is joined with new line separator`() {
        val hours = listOf(
            Times(0, 3600),
            Times(6400, 64000),
        )
        `when`(hoursService.getFormattedHours(anyList())).thenReturn(listOf("1", "2", "3"))
        assertThat(controllerV2.formatHours(hours)).isEqualTo("1\n2\n3")
    }

}