package org.exercise.openinghours.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.excercise.openinghours.Endpoints
import org.excercise.openinghours.HoursData
import org.excercise.openinghours.Times
import org.exercise.openinghours.services.HoursServiceV1
import org.exercise.openinghours.utils.Constants.DAYS_DELIMITER
import org.exercise.openinghours.utils.WrongDayOfWeekException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek

@RestController
@RequestMapping(Endpoints.V1)
@Api(tags = ["Opening Hours v1"])
class OpeningHoursControllerV1(private val hoursService: HoursServiceV1) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/formatHours")
    @ApiOperation(value = "Accepts hours as daily data. Returns open hours in human-readable format")
    fun formatHours(@RequestBody hoursMap: Map<String, List<HoursData>>): String {
        logger.info("hoursMap: $hoursMap")
        return hoursService.getFormattedHours(deserializeEnum(hoursMap)).joinToString(DAYS_DELIMITER)
    }

    @PostMapping("/convertToWeeklyFormat")
    @ApiOperation(value = "Accepts hours as daily data. Returns it as weekly data that is used in v2")
    fun convertToWeeklyFormat(@RequestBody hoursMap: Map<String, List<HoursData>>): List<Times> {
        logger.info("hoursMap: $hoursMap")
        return hoursService.convertToWeeklyHoursMap(deserializeEnum(hoursMap))
    }

    private fun deserializeEnum(hoursMap: Map<String, List<HoursData>>): Map<DayOfWeek, List<HoursData>> {
        return try {
            hoursMap.mapKeys { DayOfWeek.valueOf(it.key.uppercase()) }
        } catch (e: IllegalArgumentException) {
            throw WrongDayOfWeekException("${e.message}. Expected on of ${DayOfWeek.values().toList()}")
        }
    }

}