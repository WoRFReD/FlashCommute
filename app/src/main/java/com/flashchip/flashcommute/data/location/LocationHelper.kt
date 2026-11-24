// Version 1.02 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.location

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
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

        // 1. Si es un enlace corto de Google Maps (maps.app.goo.gl o goo.gl), resolver redirecciones
        if (input.contains("maps.app.goo.gl") || input.contains("goo.gl/maps")) {
            try {
                var currentUrl = input
                var redirects = 0
                while (redirects < 5) {
                    val url = URL(currentUrl)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 8000
                        readTimeout = 8000
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    }
                    val code = conn.responseCode
                    if (code in 300..399) {
                        val redirect = conn.getHeaderField("Location")
                        conn.disconnect()
                        if (!redirect.isNullOrBlank()) {
                            currentUrl = redirect
                            redirects++
                        } else {
                            break
                        }
                    } else {
                        conn.disconnect()
                        break
                    }
                }
                targetText = currentUrl
            } catch (e: Exception) {
                // Continuar con el texto recibido
            }
        }

        var placeNameFromUrl: String? = null

        // Extraer nombre de destino si viene en formato /dir/.../NombreDestino/...
        val dirNamePattern = Pattern.compile("/maps/dir/[^/]+/([^/?#]+)")
        val dirNameMatcher = dirNamePattern.matcher(targetText)
        if (dirNameMatcher.find()) {
            val rawName = dirNameMatcher.group(1)
            if (!rawName.isNullOrBlank() && !rawName.matches(Regex("^-?\\d+\\.\\d+,-?\\d+\\.\\d+$"))) {
                try {
                    placeNameFromUrl = URLDecoder.decode(rawName, "UTF-8").replace("+", " ")
                } catch (e: Exception) {
                    placeNameFromUrl = rawName
                }
            }
        }

        // Patrón A: Coordenadas protobuf de Google Maps !3d37.2776119!4d-5.9955691 (Rutas y Sitios)
        val protoPattern = Pattern.compile("!3d(-?\\d+\\.\\d+)!4d(-?\\d+\\.\\d+)")
        val protoMatcher = protoPattern.matcher(targetText)
        if (protoMatcher.find()) {
            val lat = protoMatcher.group(1)?.toDoubleOrNull()
            val lng = protoMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = placeNameFromUrl ?: reverseGeocode(context, lat, lng) ?: "Destino fijado desde Google Maps"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // Patrón B: Coordenadas directas de destino en ruta /dir/origen/lat,lng
        val dirCoordPattern = Pattern.compile("/maps/dir/[^/]+/(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val dirCoordMatcher = dirCoordPattern.matcher(targetText)
        if (dirCoordMatcher.find()) {
            val lat = dirCoordMatcher.group(1)?.toDoubleOrNull()
            val lng = dirCoordMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = placeNameFromUrl ?: reverseGeocode(context, lat, lng) ?: "Destino fijado desde Google Maps"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // Patrón C: Coordenadas con @ (@40.416775,-3.703790)
        val atPattern = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val atMatcher = atPattern.matcher(targetText)
        if (atMatcher.find()) {
            val lat = atMatcher.group(1)?.toDoubleOrNull()
            val lng = atMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = placeNameFromUrl ?: reverseGeocode(context, lat, lng) ?: "Ubicación en Google Maps"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // Patrón D: Parámetro q=lat,lng o destination=lat,lng o ll=lat,lng
        val qPattern = Pattern.compile("[?&](?:q|ll|destination)=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val qMatcher = qPattern.matcher(targetText)
        if (qMatcher.find()) {
            val lat = qMatcher.group(1)?.toDoubleOrNull()
            val lng = qMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = placeNameFromUrl ?: reverseGeocode(context, lat, lng) ?: "Ubicación en Google Maps"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // Patrón E: Coordenadas directas escritas "37.2776, -5.9955"
        val coordPattern = Pattern.compile("^(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)$")
        val coordMatcher = coordPattern.matcher(targetText)
        if (coordMatcher.find()) {
            val lat = coordMatcher.group(1)?.toDoubleOrNull()
            val lng = coordMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val name = reverseGeocode(context, lat, lng) ?: "Coordenadas fijadas"
                return@withContext ParsedLocation(lat, lng, name)
            }
        }

        // 3. Fallback a Geocoder nativo si es una dirección o texto
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(input, 1)
            if (!results.isNullOrEmpty()) {
                val addr = results[0]
                val readable = addr.getAddressLine(0) ?: input
                return@withContext ParsedLocation(addr.latitude, addr.longitude, readable)
            }
        } catch (e: Exception) {
            // Geocoder fallo silencioso
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
