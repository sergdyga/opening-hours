package org.excercise.openinghours

/**
 * For V2 HouseService
 */
data class Times(
    val from: Long,
    val to: Long
) {
    init {
        if (from >= to) throw CloseHourBeforeOpenHourException("Closing time $to is before or same as opening time $from")
    }
}