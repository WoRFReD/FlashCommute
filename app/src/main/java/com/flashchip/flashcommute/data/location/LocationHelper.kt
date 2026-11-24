// Version 1.01 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern

object LocationHelper {

    data class ParsedLocation(
        val latitude: Double,
        val longitude: Double,
        val addressName: String
    )

    suspend fun parseMapsInput(context: Context, rawInput: String): ParsedLocation? = withContext(Dispatchers.IO) {
        val input = rawInput.trim()
        if (input.isBlank()) return@withContext null

        var targetText = input

        // 1. Si es un enlace corto de Google Maps (maps.app.goo.gl), seguir la redirección
        if (input.contains("maps.app.goo.gl") || input.contains("goo.gl/maps")) {
            try {
                val url = URL(input)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }
                val redirect = conn.getHeaderField("Location")
                conn.disconnect()
                if (!redirect.isNullOrBlank()) {
                    targetText = redirect
                }
            } catch (e: Exception) {
                // Si falla la red, continuamos con el texto original
            }
        }

        // 2. Extraer coordenadas con expresiones regulares
        // Patrón 1: @40.416775,-3.703790
        val atPattern = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val atMatcher = atPattern.matcher(targetText)
        if (atMatcher.find()) {
            val lat = atMatcher.group(1)?.toDoubleOrNull()
            val lng = atMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = reverseGeocode(context, lat, lng) ?: "Ubicación seleccionada en Google Maps"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // Patrón 2: ?q=40.416775,-3.703790 o ll=40.416775,-3.703790 o destination=40.416775,-3.703790
        val qPattern = Pattern.compile("[?&](?:q|ll|destination)=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val qMatcher = qPattern.matcher(targetText)
        if (qMatcher.find()) {
            val lat = qMatcher.group(1)?.toDoubleOrNull()
            val lng = qMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = reverseGeocode(context, lat, lng) ?: "Ubicación seleccionada en Google Maps"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // Patrón 3: Coordenadas directas "40.416775, -3.703790"
        val coordPattern = Pattern.compile("^(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)$")
        val coordMatcher = coordPattern.matcher(targetText)
        if (coordMatcher.find()) {
            val lat = coordMatcher.group(1)?.toDoubleOrNull()
            val lng = coordMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = reverseGeocode(context, lat, lng) ?: "Ubicación fija"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // 3. Si no son coordenadas, intentar resolverlo como dirección con Geocoder nativo
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Geocoder sincrónico para la corrutina IO
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(input, 1)
                if (!results.isNullOrEmpty()) {
                    val addr = results[0]
                    val readable = addr.getAddressLine(0) ?: input
                    return@withContext ParsedLocation(addr.latitude, addr.longitude, readable)
                }
            } else {
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(input, 1)
                if (!results.isNullOrEmpty()) {
                    val addr = results[0]
                    val readable = addr.getAddressLine(0) ?: input
                    return@withContext ParsedLocation(addr.latitude, addr.longitude, readable)
                }
            }
        } catch (e: Exception) {
            // Geocoder no disponible
        }

        null
    }

    private fun reverseGeocode(context: Context, lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                val addr = list[0]
                addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}, ${addr.locality ?: ""}".trim()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
