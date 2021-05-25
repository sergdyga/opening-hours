package org.exercise.openinghours.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.excercise.openinghours.HoursData
import org.excercise.openinghours.OpenClose.close
import org.excercise.openinghours.OpenClose.open
import org.excercise.openinghours.Times
import org.exercise.openinghours.controllers.OpeningHoursControllerV1
import org.exercise.openinghours.services.HoursServiceV1Impl
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.DayOfWeek

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class OpeningHoursControllerV1ITest {

    private val mapper = ObjectMapper()
    private val hoursService = mock(HoursServiceV1Impl::class.java)
    private val mockMvc = MockMvcBuilders
        .standaloneSetup(OpeningHoursControllerV1(hoursService))
        .build()

    // To validate Spring service exceptions handling
    private val mockMvcRealService = MockMvcBuilders
        .standaloneSetup(OpeningHoursControllerV1(HoursServiceV1Impl(DayOfWeek.MONDAY)))
        .build()

    private val hoursMap = mutableMapOf(
        "SUNDAY" to listOf(HoursData(open, 0), HoursData(close, 86399)),
        "monday" to listOf(
            HoursData(open, 84000),
            HoursData(close, 64800),
            HoursData(open, 24000),
            HoursData(open, 37800),
            HoursData(close, 36000),
        ),
        "Tuesday" to listOf(HoursData(close, 21600)),
        "WEDNESDAY" to listOf(HoursData(open, 0), HoursData(close, 86399)),
        "THURSDAY" to listOf(HoursData(open, 0), HoursData(close, 86399)),
        // Friday missing
        "saturdaY" to listOf(HoursData(open, 0), HoursData(close, 86399)),
    )

    val formatResponseStub = listOf("Monday: 1-2, 3-4", "Tuesday: Closed", "Sunday: 1-12")
    val convertResponseStub = listOf(Times(36000, 64700), Times(43000, 65100))

    @BeforeAll
    fun initMocks() {
        `when`(hoursService.getFormattedHours(anyMap())).thenReturn(formatResponseStub)
        `when`(hoursService.convertToWeeklyHoursMap(anyMap())).thenReturn(convertResponseStub)
    }

    @DisplayName("/formatHours with correct body")
    @Test
    fun formatHoursWithCorrectMessageBody() {
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/formatHours")
            .content(mapper.writeValueAsString(hoursMap))
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        verify(hoursService).getFormattedHours(hoursMap.mapKeys { DayOfWeek.valueOf(it.key.uppercase()) })
        assertThat(result.response.contentAsString).isEqualTo(formatResponseStub.joinToString("\n"))
    }

    @DisplayName("/formatHours with wrong day of week return 4xx")
    @Test
    fun formatHoursWithWrongDayOfWeekReturns400() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/formatHours")
            .content("""{Monday123": []}""")
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @DisplayName("/convertToWeeklyFormat with correct body")
    @Test
    fun convertToWeeklyFormatWithCorrectMessageBody() {
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/convertToWeeklyFormat")
            .content(mapper.writeValueAsString(hoursMap))
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        verify(hoursService).convertToWeeklyHoursMap(hoursMap.mapKeys { DayOfWeek.valueOf(it.key.uppercase()) })
        assertThat(result.response.contentAsString).isEqualTo(mapper.writeValueAsString(convertResponseStub))
    }

    @DisplayName("/formatHours with missing close time return 4xx")
    @Test
    fun formatHoursWithMissingOpenClosePairReturns400() {
        mockMvcRealService.perform(MockMvcRequestBuilders.post("/v2/formatHours")
            .content("""{"monday": {"type": open, "value": 3600}""")
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

}