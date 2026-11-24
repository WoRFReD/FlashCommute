// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class LocationProvider(context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            // Intento 1: Obtener la posición fresca con alta precisión
            val cts = CancellationTokenSource()
            val freshLocation = withTimeoutOrNull(7000L) {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            }
            if (freshLocation != null) return freshLocation

            // Intento 2: Fallback a última ubicación conocida
            fusedClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }
}
