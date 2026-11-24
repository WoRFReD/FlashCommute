// Version 1.04 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.model

import java.util.Calendar

data class AppSettings(
    val workAddress: String = "Toscano",
    val workLatitude: Double = 37.277612,
    val workLongitude: Double = -5.995569,
    val bluetoothDeviceAddress: String = "",
    val bluetoothDeviceName: String = "",
    val startHour: Int = 6,
    val startMinute: Int = 0,
    val endHour: Int = 9,
    val endMinute: Int = 0,
    val activeDays: Set<Int> = setOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY
    ),
    val criticalDelayMinutes: Int = 7,
    val trafficProvider: String = "HYBRID",
    val tomtomApiKey: String = "",
    val googleApiKey: String = "",
    val lastCheckReport: TrafficReport? = null,
    val lastCheckTime: Long = 0L,
    val autoCheckEnabled: Boolean = true
) {
    fun isTimeWindowActive(now: Calendar = Calendar.getInstance()): Boolean {
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        if (!activeDays.contains(currentDay)) return false

        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return currentMinutes in startTotalMinutes..endTotalMinutes
    }
}
