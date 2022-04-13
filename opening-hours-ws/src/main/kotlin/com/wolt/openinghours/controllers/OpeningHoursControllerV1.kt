package com.wolt.openinghours.controllers

import com.wolt.openinghours.Endpoints
import com.wolt.openinghours.HoursData
import com.wolt.openinghours.services.HoursServiceV1
import com.wolt.openinghours.utils.Constants.DAYS_DELIMITER
import com.wolt.openinghours.utils.WrongDayOfWeekException
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
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

    private fun deserializeEnum(hoursMap: Map<String, List<HoursData>>): Map<DayOfWeek, List<HoursData>> {
        return try {
            hoursMap.mapKeys { DayOfWeek.valueOf(it.key.uppercase()) }
        } catch (e: IllegalArgumentException) {
            throw WrongDayOfWeekException("${e.message}. Expected on of ${DayOfWeek.values().toList()}")
        }
    }

}