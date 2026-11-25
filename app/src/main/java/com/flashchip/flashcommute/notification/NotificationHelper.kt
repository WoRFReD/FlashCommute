// Version 1.06 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.flashchip.flashcommute.MainActivity
import com.flashchip.flashcommute.data.model.TrafficReport
import com.flashchip.flashcommute.data.model.TrafficStatus

object NotificationHelper {

    const val CHANNEL_CRITICAL = "flashcommute_critical_alerts"
    const val CHANNEL_STATUS = "flashcommute_status_v2"
    const val NOTIFICATION_ID_CRITICAL = 1001
    const val NOTIFICATION_ID_STATUS = 1002
    const val NOTIFICATION_ID_SERVICE = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal 1: Alertas Críticas (Accidentes / Retrasos severos > límite configurado)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "Alertas Críticas de Tráfico",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos urgentes de accidentes y retrasos imprevistos en tu ruta al trabajo"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build()
                setSound(alarmSound, audioAttributes)
            }

            // Canal 2: Estado de Ruta (Verde y Amarillo con despertar de pantalla y notificación visible en soporte)
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "Estado de Tráfico en Tiempo Real",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos visibles en pantalla de bloqueo al conectar el Bluetooth del coche"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(defaultSound, audioAttributes)
            }

            manager.createNotificationChannel(criticalChannel)
            manager.createNotificationChannel(statusChannel)
        }
    }

    /**
     * Enciende la pantalla del móvil durante unos segundos para que se vea
     * inmediatamente en el soporte/trípode del coche sin tocar el móvil.
     */
    private fun wakeUpScreen(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                @Suppress("DEPRECATION")
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                    "flashcommute:screen_wake"
                )
                wakeLock.acquire(8000L) // Mantiene la pantalla encendida 8 segundos
            }
        } catch (e: Exception) {
            // Ignorar en caso de restricciones específicas de fabricante
        }
    }

    fun showTrafficAlert(context: Context, report: TrafficReport) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 1. Encender la pantalla del móvil para verla en el trípode/soporte
        wakeUpScreen(context)

        val isCritical = report.status == TrafficStatus.CRITICAL
        val isModerate = report.status == TrafficStatus.MODERATE

        val notification = when {
            isCritical -> {
                NotificationCompat.Builder(context, CHANNEL_CRITICAL)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("🚨 ¡Alerta Tráfico Crítico! (+${report.delayMinutes} min)")
                    .setContentText("${report.currentDurationMinutes} min al trabajo • Llegada: ${report.formattedEstimatedArrival}")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("🚨 Retraso imprevisto en tu ruta habitual:\n⏱️ Tiempo total: ${report.currentDurationMinutes} min (Retraso: +${report.delayMinutes} min)\n🏁 Llegada estimada: ${report.formattedEstimatedArrival}\n📍 Origen: ${report.originDescription}\n⚠️ ${report.summary}")
                    )
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build()
            }
            isModerate -> {
                NotificationCompat.Builder(context, CHANNEL_STATUS)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle("🟡 Tráfico Moderado: ${report.currentDurationMinutes} min (+${report.delayMinutes} min)")
                    .setContentText("Llegada: ${report.formattedEstimatedArrival} • ${report.summary}")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("🟡 Algo más lento de lo habitual:\n⏱️ Tiempo total: ${report.currentDurationMinutes} min (+${report.delayMinutes} min de demora)\n🏁 Llegada estimada: ${report.formattedEstimatedArrival}\n📍 Origen: ${report.originDescription}\nℹ️ ${report.summary}")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build()
            }
            else -> {
                // VERDE: Ruta fluida
                NotificationCompat.Builder(context, CHANNEL_STATUS)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle("🟢 Vía Libre: ${report.currentDurationMinutes} min hacia el Trabajo")
                    .setContentText("Llegada: ${report.formattedEstimatedArrival} • Tráfico fluido sin retenciones")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("🟢 Ruta fluida y despejada:\n⏱️ Duración: ${report.currentDurationMinutes} min (${report.distanceKm} km)\n🏁 Llegada estimada: ${report.formattedEstimatedArrival}\n📍 Origen: ${report.originDescription}\n✅ ${report.summary}")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build()
            }
        }

        manager.notify(if (isCritical) NOTIFICATION_ID_CRITICAL else NOTIFICATION_ID_STATUS, notification)
    }

    fun buildForegroundNotification(context: Context, statusText: String): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("FlashCommute")
            .setContentText(statusText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()
    }
}
