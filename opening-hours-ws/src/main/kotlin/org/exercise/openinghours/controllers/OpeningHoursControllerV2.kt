package org.exercise.openinghours.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.excercise.openinghours.Endpoints
import org.excercise.openinghours.Times
import org.exercise.openinghours.services.HoursServiceV2
import org.exercise.openinghours.utils.Constants.DAYS_DELIMITER
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Endpoints.V2)
@Api(tags = ["Opening Hours v2"])
class OpeningHoursControllerV2(private val hoursService: HoursServiceV2) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/formatHours")
    @ApiOperation(value = "Accepts hours as flat weekly data. Returns open hours in human-readable format")
    fun formatHours(@RequestBody hoursList: List<Times>): String {
        logger.info("hoursList: $hoursList")
        return hoursService.getFormattedHours(hoursList).joinToString(DAYS_DELIMITER)
    }

}