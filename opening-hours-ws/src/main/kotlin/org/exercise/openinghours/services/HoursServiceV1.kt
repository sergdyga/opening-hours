package org.exercise.openinghours.services

import org.excercise.openinghours.HoursData
import org.excercise.openinghours.Times
import java.time.DayOfWeek

interface HoursServiceV1 {

    /**
     * @param hoursMap - list of open-close times for each day of week
     *
     * @return opening hours in 12-hours clock human readable format
     * Response format:
     *  ...
     *  FRIDAY: 6:00 pm - 12:30 am
     *  SATURDAY: 9:00 am - 11:00 am, 4:30 om - 11:30 pm
     *  SUNDAY: Closed
     */
    fun getFormattedHours(hoursMap: Map<DayOfWeek, List<HoursData>>): List<String>

    /**
     * Converts from input data format of [HoursServiceV1] (hours/day)
     * into input data format of [HoursServiceV2] (hours/week)
     */
    fun convertToWeeklyHoursMap(hoursMap: Map<DayOfWeek, List<HoursData>>): List<Times>

}