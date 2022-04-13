package com.wolt.openinghours.utils

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class WrongTimestampException(override val message: String) : RuntimeException()

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class WrongDayOfWeekException(override val message: String) : RuntimeException()

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class MissingOpenCloseHoursPairException(override val message: String) : RuntimeException()