// Version 1.00 - FlashCommute
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
import androidx.core.app.NotificationCompat
import com.flashchip.flashcommute.MainActivity
import com.flashchip.flashcommute.data.model.TrafficReport
import com.flashchip.flashcommute.data.model.TrafficStatus

object NotificationHelper {

    const val CHANNEL_CRITICAL = "flashcommute_critical_alerts"
    const val CHANNEL_STATUS = "flashcommute_status_updates"
    const val NOTIFICATION_ID_CRITICAL = 1001
    const val NOTIFICATION_ID_STATUS = 1002
    const val NOTIFICATION_ID_SERVICE = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal 1: Alertas Críticas (Accidentes / Retrasos severos)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "Alertas Críticas de Tráfico",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos urgentes de accidentes y retrasos imprevistos en tu ruta al trabajo"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build()
                setSound(alarmSound, audioAttributes)
            }

            // Canal 2: Estado Fluido / Informativo (Silencioso y discreto)
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "Estado de Ruta y Tráfico",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Información silenciosa del tiempo estimado cuando el tráfico es fluido"
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(criticalChannel)
            manager.createNotificationChannel(statusChannel)
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

        val isCritical = report.status == TrafficStatus.CRITICAL

        val notification = if (isCritical) {
            NotificationCompat.Builder(context, CHANNEL_CRITICAL)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("🚨 ¡Atención! Tráfico en tu ruta habitual")
                .setContentText(report.summary)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${report.summary}\n\n⏱️ Tiempo total: ${report.currentDurationMinutes} min (Retraso: +${report.delayMinutes} min)\n🏁 Llegada estimada: ${report.formattedEstimatedArrival}\nℹ️ Motor: ${report.providerUsed}")
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
        } else {
            NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("🟢 Vía Libre hacia el Trabajo")
                .setContentText("${report.currentDurationMinutes} min • Llegada est.: ${report.formattedEstimatedArrival}")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Ruta fluida sin incidentes graves.\n⏱️ Duración: ${report.currentDurationMinutes} min (${report.distanceKm} km)\n🏁 Llegada estimada: ${report.formattedEstimatedArrival}")
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
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
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()
    }
}
