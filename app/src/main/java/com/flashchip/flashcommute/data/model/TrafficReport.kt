// Version 1.03 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.model

enum class TrafficStatus {
    FLUID,      // 🟢 Tráfico despejado o demora mínima
    MODERATE,   // 🟡 Algo de congestión pero asumible
    CRITICAL    // 🔴 Accidente, vía cortada o demora >= umbral crítico
}

data class TrafficReport(
    val status: TrafficStatus = TrafficStatus.FLUID,
    val normalDurationMinutes: Int = 0,
    val currentDurationMinutes: Int = 0,
    val delayMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val summary: String = "",
    val providerUsed: String = "TomTom",
    val timestamp: Long = System.currentTimeMillis(),
    val incidentList: List<String> = emptyList(),
    val errorMessage: String? = null,
    val originDescription: String = "GPS en vivo"
) {
    val isCritical: Boolean
        get() = status == TrafficStatus.CRITICAL

    val formattedEstimatedArrival: String
        get() {
            val etaMillis = timestamp + (currentDurationMinutes * 60 * 1000L)
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = etaMillis }
            return String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
        }
}
