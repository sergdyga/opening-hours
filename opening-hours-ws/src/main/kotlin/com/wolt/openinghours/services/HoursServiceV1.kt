package com.wolt.openinghours.services

import com.wolt.openinghours.HoursData
import java.time.DayOfWeek

interface HoursServiceV1 {

    /**
     * @param hoursMap - list of open-close times for each day of week
     *
     * @return opening hours in 12-hours clock human-readable format
     * Response format:
     *  ...
     *  FRIDAY: 6:00 pm - 12:30 am
     *  SATURDAY: 9:00 am - 11:00 am, 4:30 om - 11:30 pm
     *  SUNDAY: Closed
     */
    fun getFormattedHours(hoursMap: Map<DayOfWeek, List<HoursData>>): List<String>

}