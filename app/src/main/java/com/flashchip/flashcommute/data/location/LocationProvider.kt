// Version 1.07 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.flashchip.flashcommute.data.preferences.SettingsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

class LocationProvider(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val settingsManager = SettingsManager(context)

    fun isLocationServiceEnabled(): Boolean {
        val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        var foundLocation: Location? = null

        // 1. Guardar la última ubicación conocida del sistema como candidato rápido
        var systemLastLocation: Location? = null
        try {
            val fusedLast = fusedClient.lastLocation.await()
            if (fusedLast != null) {
                systemLastLocation = fusedLast
                // Si es reciente (menos de 5 min), usarla de inmediato para máxima rapidez
                if (isLocationRecent(fusedLast, 5 * 60 * 1000L)) {
                    foundLocation = fusedLast
                }
            }
        } catch (e: Exception) {
            // Continuar
        }

        // 2. Si no hay posición reciente, pedir ubicación fresca balanceada (antenas/wifi/satélite) rápido (3s)
        if (foundLocation == null) {
            val ctsBalanced = CancellationTokenSource()
            try {
                foundLocation = withTimeoutOrNull(3000L) {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, ctsBalanced.token).await()
                }
            } catch (e: Exception) {
                // Continuar
            } finally {
                ctsBalanced.cancel()
            }
        }

        // 3. Si sigue sin responder, intentar alta precisión (3s)
        if (foundLocation == null) {
            val ctsGps = CancellationTokenSource()
            try {
                foundLocation = withTimeoutOrNull(3000L) {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ctsGps.token).await()
                }
            } catch (e: Exception) {
                // Continuar
            } finally {
                ctsGps.cancel()
            }
        }

        // 4. Fallback a LocationManager nativo (Network, GPS o Pasivo)
        if (foundLocation == null) {
            try {
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locManager != null) {
                    val netLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val gpsLoc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val passiveLoc = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    foundLocation = netLoc ?: gpsLoc ?: passiveLoc
                }
            } catch (e: Exception) {
                // Continuar
            }
        }

        // 5. Fallback a la última posición conocida del sistema aunque sea más antigua
        if (foundLocation == null && systemLastLocation != null) {
            foundLocation = systemLastLocation
        }

        // 6. Red de seguridad final para garajes subterráneos: memoria persistente de la app
        if (foundLocation == null) {
            val savedCoords = settingsManager.getLastKnownLocation()
            if (savedCoords != null) {
                foundLocation = Location("garage_cache").apply {
                    latitude = savedCoords.first
                    longitude = savedCoords.second
                    time = System.currentTimeMillis()
                }
            }
        }

        // Guardar la posición obtenida para futuros rescates en zonas sin cobertura
        if (foundLocation != null && foundLocation.provider != "garage_cache") {
            settingsManager.saveLastKnownLocation(foundLocation.latitude, foundLocation.longitude)
        }

        return foundLocation
    }

    private fun isLocationRecent(location: Location, maxAgeMillis: Long): Boolean {
        val age = abs(System.currentTimeMillis() - location.time)
        return age <= maxAgeMillis
    }
}
