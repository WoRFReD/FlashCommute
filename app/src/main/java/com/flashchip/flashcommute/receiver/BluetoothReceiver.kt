// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.receiver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.flashchip.flashcommute.data.preferences.SettingsManager
import com.flashchip.flashcommute.service.TrafficCheckService

class BluetoothReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return

        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val settingsManager = SettingsManager(context)
        val settings = settingsManager.getSettings()

        if (!settings.autoCheckEnabled) return

        // 1. Verificar si estamos en la franja horaria configurada
        if (!settings.isTimeWindowActive()) return

        // 2. Verificar si el dispositivo coincide con el coche configurado
        val targetAddress = settings.bluetoothDeviceAddress
        val matchesDevice = when {
            targetAddress.isBlank() -> true // Si no configuró uno específico, actúa con cualquiera
            device?.address.equals(targetAddress, ignoreCase = true) -> true
            else -> false
        }

        if (matchesDevice) {
            // Evitar comprobaciones duplicadas si se reconecta en menos de 10 minutos
            val now = System.currentTimeMillis()
            if (now - settings.lastCheckTime < 10 * 60 * 1000L) {
                return
            }

            TrafficCheckService.startCheck(context, manual = false)
        }
    }
}
