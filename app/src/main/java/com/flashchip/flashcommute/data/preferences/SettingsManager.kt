// Version 1.04 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.flashchip.flashcommute.data.model.AppSettings
import com.flashchip.flashcommute.data.model.TrafficReport
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): AppSettings = loadSettings()

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString(KEY_WORK_ADDRESS, newSettings.workAddress)
            putFloat(KEY_WORK_LAT, newSettings.workLatitude.toFloat())
            putFloat(KEY_WORK_LNG, newSettings.workLongitude.toFloat())
            putString(KEY_BT_ADDRESS, newSettings.bluetoothDeviceAddress)
            putString(KEY_BT_NAME, newSettings.bluetoothDeviceName)
            putInt(KEY_START_HOUR, newSettings.startHour)
            putInt(KEY_START_MIN, newSettings.startMinute)
            putInt(KEY_END_HOUR, newSettings.endHour)
            putInt(KEY_END_MIN, newSettings.endMinute)
            putStringSet(KEY_ACTIVE_DAYS, newSettings.activeDays.map { it.toString() }.toSet())
            putInt(KEY_CRITICAL_DELAY, newSettings.criticalDelayMinutes)
            putString(KEY_PROVIDER, newSettings.trafficProvider)
            putString(KEY_TOMTOM_KEY, newSettings.tomtomApiKey)
            putString(KEY_GOOGLE_KEY, newSettings.googleApiKey)
            putBoolean(KEY_AUTO_ENABLED, newSettings.autoCheckEnabled)
            apply()
        }
        _settingsFlow.value = newSettings
    }

    fun saveLastReport(report: TrafficReport) {
        val json = gson.toJson(report)
        prefs.edit()
            .putString(KEY_LAST_REPORT, json)
            .putLong(KEY_LAST_TIME, report.timestamp)
            .apply()

        _settingsFlow.value = _settingsFlow.value.copy(
            lastCheckReport = report,
            lastCheckTime = report.timestamp
        )
    }

    fun saveLastKnownLocation(lat: Double, lng: Double) {
        prefs.edit()
            .putFloat(KEY_SAVED_LAT, lat.toFloat())
            .putFloat(KEY_SAVED_LNG, lng.toFloat())
            .putLong(KEY_SAVED_LOC_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getLastKnownLocation(): Pair<Double, Double>? {
        if (!prefs.contains(KEY_SAVED_LAT) || !prefs.contains(KEY_SAVED_LNG)) return null
        val lat = prefs.getFloat(KEY_SAVED_LAT, 0f).toDouble()
        val lng = prefs.getFloat(KEY_SAVED_LNG, 0f).toDouble()
        if (lat == 0.0 && lng == 0.0) return null
        return Pair(lat, lng)
    }

    private fun loadSettings(): AppSettings {
        val daysSet = prefs.getStringSet(KEY_ACTIVE_DAYS, null)?.mapNotNull {
            it.toIntOrNull()
        }?.toSet() ?: setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY
        )

        val reportJson = prefs.getString(KEY_LAST_REPORT, null)
        val lastReport = if (reportJson != null) {
            try {
                gson.fromJson(reportJson, TrafficReport::class.java)
            } catch (e: Exception) {
                null
            }
        } else null

        return AppSettings(
            workAddress = prefs.getString(KEY_WORK_ADDRESS, "Toscano") ?: "Toscano",
            workLatitude = prefs.getFloat(KEY_WORK_LAT, 37.277612f).toDouble(),
            workLongitude = prefs.getFloat(KEY_WORK_LNG, -5.995569f).toDouble(),
            bluetoothDeviceAddress = prefs.getString(KEY_BT_ADDRESS, "") ?: "",
            bluetoothDeviceName = prefs.getString(KEY_BT_NAME, "") ?: "",
            startHour = prefs.getInt(KEY_START_HOUR, 6),
            startMinute = prefs.getInt(KEY_START_MIN, 0),
            endHour = prefs.getInt(KEY_END_HOUR, 9),
            endMinute = prefs.getInt(KEY_END_MIN, 0),
            activeDays = daysSet,
            criticalDelayMinutes = prefs.getInt(KEY_CRITICAL_DELAY, 7),
            trafficProvider = prefs.getString(KEY_PROVIDER, "HYBRID") ?: "HYBRID",
            tomtomApiKey = prefs.getString(KEY_TOMTOM_KEY, "") ?: "",
            googleApiKey = prefs.getString(KEY_GOOGLE_KEY, "") ?: "",
            lastCheckReport = lastReport,
            lastCheckTime = prefs.getLong(KEY_LAST_TIME, 0L),
            autoCheckEnabled = prefs.getBoolean(KEY_AUTO_ENABLED, true)
        )
    }

    companion object {
        private const val PREFS_NAME = "flashcommute_preferences"
        private const val KEY_WORK_ADDRESS = "work_address"
        private const val KEY_WORK_LAT = "work_lat"
        private const val KEY_WORK_LNG = "work_lng"
        private const val KEY_BT_ADDRESS = "bt_address"
        private const val KEY_BT_NAME = "bt_name"
        private const val KEY_START_HOUR = "start_hour"
        private const val KEY_START_MIN = "start_min"
        private const val KEY_END_HOUR = "end_hour"
        private const val KEY_END_MIN = "end_min"
        private const val KEY_ACTIVE_DAYS = "active_days"
        private const val KEY_CRITICAL_DELAY = "critical_delay"
        private const val KEY_PROVIDER = "traffic_provider"
        private const val KEY_TOMTOM_KEY = "tomtom_key"
        private const val KEY_GOOGLE_KEY = "google_key"
        private const val KEY_LAST_REPORT = "last_report"
        private const val KEY_LAST_TIME = "last_time"
        private const val KEY_AUTO_ENABLED = "auto_enabled"
        private const val KEY_SAVED_LAT = "saved_last_lat"
        private const val KEY_SAVED_LNG = "saved_last_lng"
        private const val KEY_SAVED_LOC_TIME = "saved_last_loc_time"
    }
}
