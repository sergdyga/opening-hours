package org.exercise.openinghours.services

import org.excercise.openinghours.Times

interface HoursServiceV2 {

    /**
     * @param hoursList - list of open-close time pairs for 1 week
     *
     * @return opening hours in 12-hours clock human readable format
     * Response format:
     *  ...
     *  FRIDAY: 6:00 pm - 12:30 am
     *  SATURDAY: 9:00 am - 11:00 am, 4:30 om - 11:30 pm
     *  SUNDAY: Closed
     */
    fun getFormattedHours(hoursList: List<Times>): List<String>

}