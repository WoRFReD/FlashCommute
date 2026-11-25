// Version 1.06 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.flashchip.flashcommute.ui.theme.*

@Composable
fun PermissionsBanner() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun hasPermission(perm: String): Boolean =
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED

    fun checkNormalPermissions(): Boolean {
        val fineLoc = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val btConnect = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        val notif = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        return fineLoc && btConnect && notif
    }

    fun checkBackgroundLocation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        }
    }

    var hasNormal by remember { mutableStateOf(checkNormalPermissions()) }
    var hasBackground by remember { mutableStateOf(checkBackgroundLocation()) }

    // Actualizar estado al volver de la pantalla de Ajustes del sistema
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNormal = checkNormalPermissions()
                hasBackground = checkBackgroundLocation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val normalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasNormal = checkNormalPermissions()
        hasBackground = checkBackgroundLocation()
    }

    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        hasBackground = checkBackgroundLocation()
    }

    // Caso 1: Faltan permisos básicos (Ubicación, Bluetooth o Notificaciones)
    if (!hasNormal) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.dp, FlashAmber, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = FlashAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permisos necesarios para automatización",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlashAmber
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Para detectar la conexión con tu coche y avisarte del tráfico, FlashCommute necesita permisos de Ubicación, Bluetooth y Notificaciones.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        normalLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlashAmber, contentColor = BgDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("1. Conceder Permisos Iniciales", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
    // Caso 2: Tiene permisos normales pero falta "Permitir todo el tiempo" (Segundo plano)
    else if (!hasBackground) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.dp, FlashCyan, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = FlashCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acceso con Pantalla Apagada en el Coche",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlashCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Para que la app detecte el GPS cuando te subes al coche con el móvil en el bolsillo o bloqueado, selecciona 'Permitir todo el tiempo' en los ajustes de ubicación.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                // En Android 11+, abrir directamente la pantalla de permisos de la app en Ajustes
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlashCyan, contentColor = BgDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Activar 'Permitir todo el tiempo'", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
