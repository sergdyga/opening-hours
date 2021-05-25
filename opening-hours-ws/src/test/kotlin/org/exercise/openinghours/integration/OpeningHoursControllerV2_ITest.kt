package org.exercise.openinghours.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.excercise.openinghours.Times
import org.exercise.openinghours.controllers.OpeningHoursControllerV2
import org.exercise.openinghours.services.HoursServiceV2Impl
import org.exercise.openinghours.utils.andExpectWithDebug
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
class OpeningHoursControllerV2ITest {

    private val mapper = ObjectMapper()
    private val hoursService = mock(HoursServiceV2Impl::class.java)
    private val mockMvc = MockMvcBuilders
        .standaloneSetup(OpeningHoursControllerV2(hoursService))
        .build()

    // To validate Spring service exceptions handling
    private val mockMvcRealService = MockMvcBuilders
        .standaloneSetup(OpeningHoursControllerV2(HoursServiceV2Impl(DayOfWeek.MONDAY)))
        .build()

    private val hoursList = listOf(
        Times(30000, 37800),
        Times(45000, 59400),
        Times(70200, 81000),
        Times(172800, 259199),
        Times(259200, 345599),
        Times(345600, 431999),
        Times(432000, 518399),
        Times(518400, 604799),
    )

    val formatResponseStub = listOf("Monday: 1-2, 3-4", "Tuesday: Closed", "Sunday: 1-12")

    @BeforeAll
    fun initMocks() {
        `when`(hoursService.getFormattedHours(hoursList)).thenReturn(formatResponseStub)
    }

    @DisplayName("/formatHours with correct body")
    @Test
    fun formatHoursWithCorrectMessageBody() {
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v2/formatHours")
            .content(mapper.writeValueAsString(hoursList))
            .contentType("application/json"))
            .andExpectWithDebug(MockMvcResultMatchers.status().isOk)
            .andReturn()

        verify(hoursService).getFormattedHours(hoursList)
        assertThat(result.response.contentAsString).isEqualTo(formatResponseStub.joinToString("\n"))
    }

    @DisplayName("/formatHours with inverse hours return 4xx")
    @Test
    fun formatHoursWithInverseHoursReturns400() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v2/formatHours")
            .content("""[{"from": 3600, "to": 0}]""") // Inverse order
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @DisplayName("/formatHours with wrong timestamp return 4xx")
    @Test
    fun formatHoursWithWrongTimestampReturns400() {
        mockMvcRealService.perform(MockMvcRequestBuilders.post("/v2/formatHours")
            .content("""[{"from": -1, "to": 3600}]""")
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @DisplayName("/formatHours with wrong timestamp return 4xx")
    @Test
    fun formatHoursWithMissingOpenClosePairReturns400() {
        mockMvcRealService.perform(MockMvcRequestBuilders.post("/v2/formatHours")
            .content("""[{"from": -1, "to": 3600}]""")
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @DisplayName("/formatHours with overlapping hours return 4xx")
    @Test
    fun formatHoursWithOverlappingHoursReturns400() {
        mockMvcRealService.perform(MockMvcRequestBuilders.post("/v2/formatHours")
            .content("""[{"from": 0, "to": 3600},{"from": 3599, "to": 64000}]""")
            .contentType("application/json"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

}