// Version 1.03 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class LocationProvider(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        // 1. Comprobar la última ubicación conocida del sistema (cero esperas)
        try {
            val fusedLast = fusedClient.lastLocation.await()
            if (fusedLast != null && isLocationRecent(fusedLast, 15 * 60 * 1000L)) {
                return fusedLast
            }
        } catch (e: Exception) {
            // Continuar
        }

        // 2. Comprobar LocationManager nativo (Network Provider es instantáneo en interiores)
        try {
            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locManager != null) {
                val netLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (netLoc != null && isLocationRecent(netLoc, 20 * 60 * 1000L)) {
                    return netLoc
                }
                val gpsLoc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLoc != null && isLocationRecent(gpsLoc, 20 * 60 * 1000L)) {
                    return gpsLoc
                }
            }
        } catch (e: Exception) {
            // Continuar
        }

        // 3. Solicitar posición fresca rápida por red y Wi-Fi (Priority Balanced, ideal en garajes y casas)
        try {
            val ctsBalanced = CancellationTokenSource()
            val balancedLoc = withTimeoutOrNull(4000L) {
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, ctsBalanced.token).await()
            }
            if (balancedLoc != null) return balancedLoc
        } catch (e: Exception) {
            // Continuar
        }

        // 4. Solicitar posición fresca con GPS por satélite
        try {
            val ctsGps = CancellationTokenSource()
            val gpsLoc = withTimeoutOrNull(5000L) {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ctsGps.token).await()
            }
            if (gpsLoc != null) return gpsLoc
        } catch (e: Exception) {
            // Continuar
        }

        // 5. Fallback a cualquier última ubicación conocida aunque sea más antigua
        try {
            return fusedClient.lastLocation.await()
        } catch (e: Exception) {
            return null
        }
    }

    private fun isLocationRecent(location: Location, maxAgeMillis: Long): Boolean {
        val age = System.currentTimeMillis() - location.time
        return age in 0..maxAgeMillis
    }
}
