// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.api

import com.flashchip.flashcommute.data.model.AppSettings
import com.flashchip.flashcommute.data.model.TrafficReport
import com.flashchip.flashcommute.data.model.TrafficStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrafficRepository(
    private val tomtomService: TomTomTrafficService = TomTomTrafficService(),
    private val googleService: GoogleRoutesService = GoogleRoutesService()
) {

    suspend fun fetchTraffic(
        settings: AppSettings,
        originLat: Double,
        originLng: Double
    ): TrafficReport = withContext(Dispatchers.IO) {
        val destLat = settings.workLatitude
        val destLng = settings.workLongitude
        val threshold = settings.criticalDelayMinutes

        val hasTomTom = settings.tomtomApiKey.isNotBlank()
        val hasGoogle = settings.googleApiKey.isNotBlank()

        if (!hasTomTom && !hasGoogle) {
            return@withContext TrafficReport(
                status = TrafficStatus.MODERATE,
                normalDurationMinutes = 20,
                currentDurationMinutes = 20,
                delayMinutes = 0,
                distanceKm = 15.0,
                summary = "Claves API no configuradas. Accede a Ajustes para añadir TomTom o Google.",
                providerUsed = "Modo Demo / Sin Clave",
                incidentList = listOf("Añade tu clave gratuita de TomTom o Google en Ajustes para datos reales en vivo")
            )
        }

        when (settings.trafficProvider) {
            "TOMTOM" -> {
                if (hasTomTom) {
                    try {
                        return@withContext tomtomService.getTrafficReport(
                            settings.tomtomApiKey, originLat, originLng, destLat, destLng, threshold
                        )
                    } catch (e: Exception) {
                        return@withContext createErrorReport("TomTom: ${e.message}")
                    }
                } else {
                    return@withContext createErrorReport("La clave de TomTom está vacía")
                }
            }

            "GOOGLE" -> {
                if (hasGoogle) {
                    try {
                        return@withContext googleService.getTrafficReport(
                            settings.googleApiKey, originLat, originLng, destLat, destLng, threshold
                        )
                    } catch (e: Exception) {
                        return@withContext createErrorReport("Google Routes: ${e.message}")
                    }
                } else {
                    return@withContext createErrorReport("La clave de Google Routes está vacía")
                }
            }

            else -> { // "HYBRID" (Fallback automático)
                var lastError: String? = null

                if (hasTomTom) {
                    try {
                        return@withContext tomtomService.getTrafficReport(
                            settings.tomtomApiKey, originLat, originLng, destLat, destLng, threshold
                        )
                    } catch (e: Exception) {
                        lastError = "TomTom falló (${e.message})"
                    }
                }

                if (hasGoogle) {
                    try {
                        val report = googleService.getTrafficReport(
                            settings.googleApiKey, originLat, originLng, destLat, destLng, threshold
                        )
                        return@withContext if (lastError != null) {
                            report.copy(summary = "${report.summary} (Respaldo Google tras fallo TomTom)")
                        } else report
                    } catch (e: Exception) {
                        lastError = (lastError?.plus(" | ") ?: "") + "Google falló (${e.message})"
                    }
                }

                return@withContext createErrorReport(lastError ?: "No se pudo obtener información de ninguna API")
            }
        }
    }

    suspend fun testTomTomKey(key: String): Boolean = tomtomService.testApiKey(key)

    suspend fun testGoogleKey(key: String): Boolean = googleService.testApiKey(key)

    private fun createErrorReport(message: String): TrafficReport {
        return TrafficReport(
            status = TrafficStatus.MODERATE,
            summary = "Error al consultar tráfico: $message",
            errorMessage = message,
            providerUsed = "Error"
        )
    }
}
