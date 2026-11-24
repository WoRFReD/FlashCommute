// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.api

import com.flashchip.flashcommute.data.model.TrafficReport
import com.flashchip.flashcommute.data.model.TrafficStatus
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TomTomTrafficService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
) {

    suspend fun getTrafficReport(
        apiKey: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        criticalDelayMinutes: Int
    ): TrafficReport = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("La clave API de TomTom no está configurada")
        }

        // TomTom Routing API with traffic
        val locations = String.format(java.util.Locale.US, "%.6f,%.6f:%.6f,%.6f", originLat, originLng, destLat, destLng)
        val url = "https://api.tomtom.com/routing/1/calculateRoute/$locations/json?key=$apiKey&traffic=true&travelMode=car&departAt=now"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw Exception("Error TomTom (${response.code}): $errorBody")
            }

            val body = response.body?.string() ?: throw Exception("Respuesta vacía de TomTom")
            val json = JsonParser.parseString(body).asJsonObject

            val routes = json.getAsJsonArray("routes")
            if (routes == null || routes.size() == 0) {
                throw Exception("No se encontró ninguna ruta disponible en TomTom")
            }

            val summary = routes[0].asJsonObject.getAsJsonObject("summary")
            val travelTimeSeconds = summary.get("travelTimeInSeconds")?.asInt ?: 0
            val trafficDelaySeconds = summary.get("trafficDelayInSeconds")?.asInt ?: 0
            val noTrafficTravelTimeSeconds = summary.get("noTrafficTravelTimeInSeconds")?.asInt ?: (travelTimeSeconds - trafficDelaySeconds)
            val lengthMeters = summary.get("lengthInMeters")?.asDouble ?: 0.0

            val currentMinutes = Math.max(1, (travelTimeSeconds / 60))
            val normalMinutes = Math.max(1, (noTrafficTravelTimeSeconds / 60))
            val delayMinutes = Math.max(0, (trafficDelaySeconds / 60))
            val distanceKm = Math.round((lengthMeters / 1000.0) * 10.0) / 10.0

            val incidents = mutableListOf<String>()
            val status = when {
                delayMinutes >= criticalDelayMinutes -> {
                    incidents.add("Retención de +$delayMinutes min detectada")
                    TrafficStatus.CRITICAL
                }
                delayMinutes >= 3 -> {
                    incidents.add("Tráfico moderado (+${delayMinutes} min)")
                    TrafficStatus.MODERATE
                }
                else -> {
                    incidents.add("Vía despejada sin demoras destacables")
                    TrafficStatus.FLUID
                }
            }

            val summaryText = when (status) {
                TrafficStatus.CRITICAL -> "⚠️ Alerta: Retención de +$delayMinutes min en tu ruta hacia el trabajo"
                TrafficStatus.MODERATE -> "🟡 Tráfico algo denso (+${delayMinutes} min sobre lo habitual)"
                TrafficStatus.FLUID -> "🟢 Tráfico fluido: $currentMinutes min estimados al trabajo"
            }

            TrafficReport(
                status = status,
                normalDurationMinutes = normalMinutes,
                currentDurationMinutes = currentMinutes,
                delayMinutes = delayMinutes,
                distanceKm = distanceKm,
                summary = summaryText,
                providerUsed = "TomTom Traffic",
                timestamp = System.currentTimeMillis(),
                incidentList = incidents
            )
        }
    }

    suspend fun testApiKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Test query around Madrid center
            val url = "https://api.tomtom.com/routing/1/calculateRoute/40.4168,-3.7038:40.4500,-3.6900/json?key=$apiKey&traffic=false"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }
}
