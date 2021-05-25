package org.exercise.openinghours.utils

object Constants {

    const val DAYS_DELIMITER = "\n"
    const val OPEN_CLOSE_DELIMITER = " - "
    const val TIMES_DELIMITER = ", "

    const val FULL_DAY = 86400
    const val FULL_WEEK = 604800
    const val FULL_WEEK_PLUS_DAY = FULL_WEEK + FULL_DAY // One extra day for SUNDAY late closing hours

}