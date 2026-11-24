// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.api

import com.flashchip.flashcommute.data.model.TrafficReport
import com.flashchip.flashcommute.data.model.TrafficStatus
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GoogleRoutesService(
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
            throw IllegalArgumentException("La clave API de Google Routes no está configurada")
        }

        val url = "https://routes.googleapis.com/directions/v2:computeRoutes"
        val jsonPayload = """
            {
              "origin": {
                "location": {
                  "latLng": {
                    "latitude": $originLat,
                    "longitude": $originLng
                  }
                }
              },
              "destination": {
                "location": {
                  "latLng": {
                    "latitude": $destLat,
                    "longitude": $destLng
                  }
                }
              },
              "travelMode": "DRIVE",
              "routingPreference": "TRAFFIC_AWARE_OPTIMAL"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", apiKey)
            .addHeader("X-Goog-FieldMask", "routes.duration,routes.staticDuration,routes.distanceMeters,routes.description,routes.warnings")
            .post(jsonPayload.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw Exception("Error Google Routes (${response.code}): $errorBody")
            }

            val body = response.body?.string() ?: throw Exception("Respuesta vacía de Google Routes")
            val json = JsonParser.parseString(body).asJsonObject

            val routes = json.getAsJsonArray("routes")
            if (routes == null || routes.size() == 0) {
                throw Exception("No se encontró ninguna ruta en Google Routes")
            }

            val route = routes[0].asJsonObject
            val durationStr = route.get("duration")?.asString ?: "0s"
            val staticDurationStr = route.get("staticDuration")?.asString ?: durationStr
            val distanceMeters = route.get("distanceMeters")?.asDouble ?: 0.0

            val durationSeconds = parseSeconds(durationStr)
            val staticDurationSeconds = parseSeconds(staticDurationStr)

            val currentMinutes = Math.max(1, (durationSeconds / 60))
            val normalMinutes = Math.max(1, (staticDurationSeconds / 60))
            val delayMinutes = Math.max(0, currentMinutes - normalMinutes)
            val distanceKm = Math.round((distanceMeters / 1000.0) * 10.0) / 10.0

            val incidents = mutableListOf<String>()
            route.getAsJsonArray("warnings")?.forEach { warning ->
                incidents.add(warning.asString)
            }

            val status = when {
                delayMinutes >= criticalDelayMinutes -> {
                    if (incidents.isEmpty()) incidents.add("Retención de +$delayMinutes min detectada")
                    TrafficStatus.CRITICAL
                }
                delayMinutes >= 3 -> {
                    if (incidents.isEmpty()) incidents.add("Tráfico moderado (+${delayMinutes} min)")
                    TrafficStatus.MODERATE
                }
                else -> {
                    incidents.add("Vía despejada según Google Routes")
                    TrafficStatus.FLUID
                }
            }

            val summaryText = when (status) {
                TrafficStatus.CRITICAL -> "⚠️ Alerta Google: Retención de +$delayMinutes min hacia el trabajo"
                TrafficStatus.MODERATE -> "🟡 Google: Tráfico algo denso (+${delayMinutes} min)"
                TrafficStatus.FLUID -> "🟢 Google: Tráfico fluido ($currentMinutes min al trabajo)"
            }

            TrafficReport(
                status = status,
                normalDurationMinutes = normalMinutes,
                currentDurationMinutes = currentMinutes,
                delayMinutes = delayMinutes,
                distanceKm = distanceKm,
                summary = summaryText,
                providerUsed = "Google Routes",
                timestamp = System.currentTimeMillis(),
                incidentList = incidents
            )
        }
    }

    suspend fun testApiKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "https://routes.googleapis.com/directions/v2:computeRoutes"
            val jsonPayload = """
                {
                  "origin": { "location": { "latLng": { "latitude": 40.4168, "longitude": -3.7038 } } },
                  "destination": { "location": { "latLng": { "latitude": 40.4500, "longitude": -3.6900 } } },
                  "travelMode": "DRIVE"
                }
            """.trimIndent()
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", apiKey)
                .addHeader("X-Goog-FieldMask", "routes.duration")
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    private fun parseSeconds(secondsStr: String): Int {
        val clean = secondsStr.replace("s", "").trim()
        return clean.toIntOrNull() ?: 0
    }
}
