// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.flashchip.flashcommute.ui.theme.*

@Composable
fun PermissionsBanner() {
    val context = LocalContext.current

    fun hasPermission(perm: String): Boolean =
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED

    var hasAllNeeded by remember {
        mutableStateOf(
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasPermission(Manifest.permission.POST_NOTIFICATIONS))
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasAllNeeded = results.values.all { it }
    }

    if (!hasAllNeeded) {
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
                    text = "Para detectar la conexión de tu coche y consultar el tráfico sin abrir la app, FlashCommute necesita permisos de Ubicación, Bluetooth y Notificaciones.",
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
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlashAmber, contentColor = BgDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Conceder Permisos Ahora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
