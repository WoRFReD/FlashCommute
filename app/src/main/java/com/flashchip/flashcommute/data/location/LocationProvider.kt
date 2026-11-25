// Version 1.06 - FlashCommute
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

class LocationProvider(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val settingsManager = SettingsManager(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        var foundLocation: Location? = null

        // 1. Comprobar la última ubicación de Google Play Services si es muy reciente (<= 10 min)
        try {
            val fusedLast = fusedClient.lastLocation.await()
            if (fusedLast != null && isLocationRecent(fusedLast, 10 * 60 * 1000L)) {
                foundLocation = fusedLast
            }
        } catch (e: Exception) {
            // Continuar
        }

        // 2. Si no hay posición fresca, intentar solicitar posición rápida por Red/Wi-Fi/Torres (3.5 seg)
        if (foundLocation == null) {
            try {
                val ctsBalanced = CancellationTokenSource()
                foundLocation = withTimeoutOrNull(3500L) {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, ctsBalanced.token).await()
                }
            } catch (e: Exception) {
                // Continuar
            }
        }

        // 3. Si sigue sin haber, solicitar posición por GPS satélite (4.5 seg)
        if (foundLocation == null) {
            try {
                val ctsGps = CancellationTokenSource()
                foundLocation = withTimeoutOrNull(4500L) {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ctsGps.token).await()
                }
            } catch (e: Exception) {
                // Continuar
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

        // 5. Fallback a la última ubicación del sistema aunque sea más antigua (p. ej. al aparcar anoche o entrar al garaje)
        if (foundLocation == null) {
            try {
                foundLocation = fusedClient.lastLocation.await()
            } catch (e: Exception) {
                // Continuar
            }
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

        // Guardar siempre la posición obtenida para futuros rescates en zonas sin cobertura
        if (foundLocation != null) {
            settingsManager.saveLastKnownLocation(foundLocation.latitude, foundLocation.longitude)
        }

        return foundLocation
    }

    private fun isLocationRecent(location: Location, maxAgeMillis: Long): Boolean {
        val age = System.currentTimeMillis() - location.time
        return age in 0..maxAgeMillis
    }
}
