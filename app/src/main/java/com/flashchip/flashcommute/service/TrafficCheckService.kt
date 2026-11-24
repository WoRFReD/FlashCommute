// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.flashchip.flashcommute.data.api.TrafficRepository
import com.flashchip.flashcommute.data.location.LocationProvider
import com.flashchip.flashcommute.data.preferences.SettingsManager
import com.flashchip.flashcommute.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TrafficCheckService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var settingsManager: SettingsManager
    private lateinit var locationProvider: LocationProvider
    private val trafficRepository = TrafficRepository()

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
        locationProvider = LocationProvider(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isManual = intent?.getBooleanExtra(EXTRA_MANUAL_CHECK, false) ?: false

        val foregroundNotification = NotificationHelper.buildForegroundNotification(
            this, "Comprobando tráfico en tu ruta habitual..."
        )
        try {
            startForeground(NotificationHelper.NOTIFICATION_ID_SERVICE, foregroundNotification)
        } catch (e: Exception) {
            // Android 14 foreground service requirement safe handling
        }

        serviceScope.launch {
            try {
                val settings = settingsManager.getSettings()

                // Si viene del Bluetooth (no manual), comprobar franja horaria y días
                if (!isManual && !settings.isTimeWindowActive()) {
                    stopSelf()
                    return@launch
                }

                // Obtener ubicación GPS actual
                val location = locationProvider.getCurrentLocation()
                val originLat = location?.latitude ?: 40.4168
                val originLng = location?.longitude ?: -3.7038

                // Consultar reporte de tráfico
                val report = trafficRepository.fetchTraffic(settings, originLat, originLng)

                // Guardar en preferencias para la pantalla principal
                settingsManager.saveLastReport(report)

                // Emitir notificación correspondiente
                NotificationHelper.showTrafficAlert(this@TrafficCheckService, report)

            } catch (e: Exception) {
                // Manejo silencioso ante errores de red o ubicación
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_MANUAL_CHECK = "extra_manual_check"

        fun startCheck(context: Context, manual: Boolean = false) {
            val intent = Intent(context, TrafficCheckService::class.java).apply {
                putExtra(EXTRA_MANUAL_CHECK, manual)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
