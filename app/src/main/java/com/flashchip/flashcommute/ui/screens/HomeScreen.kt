// Version 1.03 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flashchip.flashcommute.data.model.TrafficReport
import com.flashchip.flashcommute.data.model.TrafficStatus
import com.flashchip.flashcommute.data.preferences.SettingsManager
import com.flashchip.flashcommute.service.TrafficCheckService
import com.flashchip.flashcommute.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settingsManager: SettingsManager,
    onOpenSettings: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val settings by settingsManager.settingsFlow.collectAsState()
    var isCheckingManual by remember { mutableStateOf(false) }
    var showNoApiDialog by remember { mutableStateOf(false) }

    val report = settings.lastCheckReport
    val status = report?.status ?: TrafficStatus.FLUID

    val statusColor = when (status) {
        TrafficStatus.CRITICAL -> FlashRed
        TrafficStatus.MODERATE -> FlashAmber
        TrafficStatus.FLUID -> FlashGreen
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = FlashCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = FlashCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("FlashCommute", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            Text("Cockpit de Tráfico FlashChip", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGuide) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Guía APIs", tint = FlashCyan)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = TextPrimary)
                    }
                    IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Default.Info, contentDescription = "Acerca de", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. HUD PRINCIPAL: ESTADO DE LA RUTA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, statusColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge de Estado
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (status) {
                                    TrafficStatus.CRITICAL -> "🚨 ALERTA: RETENCIÓN SEVERA"
                                    TrafficStatus.MODERATE -> "🟡 TRÁFICO MODERADO"
                                    TrafficStatus.FLUID -> "🟢 VÍA LIBRE Y FLUIDA"
                                },
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tiempo Grande y Texto Claro
                    if (report != null) {
                        Text(
                            text = "${report.currentDurationMinutes}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            lineHeight = 64.sp
                        )
                        Text(
                            text = when (status) {
                                TrafficStatus.CRITICAL -> "⚠️ RETRASO CRÍTICO: TIEMPO PARA LLEGAR ${report.currentDurationMinutes} MIN"
                                TrafficStatus.MODERATE -> "🟡 MÁS DE LO HABITUAL: TIEMPO PARA LLEGAR ${report.currentDurationMinutes} MIN"
                                TrafficStatus.FLUID -> "🟢 RUTA FLUIDA: TIEMPO PARA LLEGAR ${report.currentDurationMinutes} MIN"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Métricas en 3 columnas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCard, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HudMetric(
                                label = "LLEGADA EST.",
                                value = report.formattedEstimatedArrival,
                                color = FlashCyan
                            )
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(CardBorder))
                            HudMetric(
                                label = "RETRASO",
                                value = if (report.delayMinutes > 0) "+${report.delayMinutes} min" else "0 min",
                                color = if (report.delayMinutes >= settings.criticalDelayMinutes) FlashRed else FlashGreen
                            )
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(CardBorder))
                            HudMetric(
                                label = "DISTANCIA",
                                value = "${report.distanceKm} km",
                                color = TextPrimary
                            )
                        }
                    } else {
                        // Estado inicial sin comprobaciones aún
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = FlashCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Listo para vigilar tu ruta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "La app se activará sola al conectar el Bluetooth del coche o pulsando 'Comprobar Ruta Ahora'.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (report?.summary?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = report.summary,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. INCIDENTES O AVISOS DETALLADOS
            if (report != null && report.incidentList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = FlashAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detalles e Incidentes en Ruta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        report.incidentList.forEach { incident ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = statusColor, fontWeight = FontWeight.Bold)
                                Text(incident, color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 3. TARJETA DE SINCRONIZACIÓN CON EL COCHE Y DESTINO
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("INFORMACIÓN DE TRAYECTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FlashCyan, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Origen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = FlashGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Origen: ${report?.originDescription ?: settings.homeAddress}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (report != null) "Calculado desde tu posición real" else "Ubicación de salida habitual",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Destino
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = FlashAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Destino: ${settings.workAddress}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "GPS: %.4f, %.4f", settings.workLatitude, settings.workLongitude),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = FlashCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (settings.bluetoothDeviceName.isNotBlank()) settings.bluetoothDeviceName else "Cualquier Bluetooth de coche",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Horario: %02d:%02d a %02d:%02d".format(settings.startHour, settings.startMinute, settings.endHour, settings.endMinute),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = FlashGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(8.dp).background(FlashGreen, CircleShape))
                            }
                        }
                    }

                    if (settings.lastCheckTime > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val formattedDate = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(settings.lastCheckTime))
                        Text(
                            text = "Última comprobación: $formattedDate (${report?.providerUsed ?: "Auto"})",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // 4. BOTONES DE ACCIÓN RÁPIDA
            Button(
                onClick = {
                    val hasTomTom = settings.tomtomApiKey.trim().isNotBlank()
                    val hasGoogle = settings.googleApiKey.trim().isNotBlank()

                    if (!hasTomTom && !hasGoogle) {
                        showNoApiDialog = true
                    } else {
                        isCheckingManual = true
                        TrafficCheckService.startCheck(context, manual = true)
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isCheckingManual = false
                        }, 2500)
                    }
                },
                enabled = !isCheckingManual,
                colors = ButtonDefaults.buttonColors(containerColor = FlashCyan, contentColor = BgDark),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCheckingManual) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BgDark, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consultando Tráfico en Vivo...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comprobar Ruta Ahora", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Botón directo a Google Maps para navegación
            OutlinedButton(
                onClick = {
                    val destLat = settings.workLatitude
                    val destLng = settings.workLongitude
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destLat,$destLng")).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng"))
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = FlashAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir Navegación en Google Maps", fontSize = 13.sp)
            }
        }
    }

    // Diálogo de aviso cuando no hay clave API configurada
    if (showNoApiDialog) {
        AlertDialog(
            onDismissRequest = { showNoApiDialog = false },
            containerColor = SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = FlashAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clave API Requerida", color = FlashAmber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "Para consultar el tráfico en tiempo real y calcular el tiempo exacto hacia tu trabajo, necesitas introducir tu clave gratuita de TomTom (2.500 pet./día gratis) o Google Routes.\n\nPuedes añadirla cómodamente desde Ajustes.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoApiDialog = false
                        onOpenSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlashCyan, contentColor = BgDark)
                ) {
                    Text("Ir a Ajustes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoApiDialog = false }) {
                    Text("Entendido", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun HudMetric(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
