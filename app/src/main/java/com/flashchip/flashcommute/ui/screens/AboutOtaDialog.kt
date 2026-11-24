// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flashchip.flashcommute.data.model.OtaReleaseInfo
import com.flashchip.flashcommute.data.ota.OtaRepository
import com.flashchip.flashcommute.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AboutOtaDialog(
    currentVersion: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val otaRepository = remember { OtaRepository(context) }

    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadText by remember { mutableStateOf("") }
    var releaseInfo by remember { mutableStateOf<OtaReleaseInfo?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        containerColor = SurfaceDark,
        titleContentColor = FlashCyan,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = FlashCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text("FlashCommute", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Versión instalada: v$currentVersion",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Desarrollado para FlashChip por WoRFReD",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = CardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                if (isDownloading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Descargando actualización...", color = FlashCyan, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = downloadProgress,
                            color = FlashCyan,
                            trackColor = SurfaceCard,
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(downloadText, color = TextSecondary, fontSize = 11.sp)
                    }
                } else if (releaseInfo != null && releaseInfo!!.isNewer) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FlashGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "¡Nueva versión disponible: ${releaseInfo!!.tagName}!",
                                color = FlashGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = releaseInfo!!.releaseNotes,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 6
                        )
                    }
                } else {
                    if (statusMessage != null) {
                        Text(statusMessage!!, color = FlashAmber, fontSize = 13.sp)
                    } else {
                        Text(
                            "Comprueba si hay nuevas versiones publicadas en GitHub con mejoras de rutas y tráfico.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (releaseInfo != null && releaseInfo!!.isNewer && !isDownloading) {
                Button(
                    onClick = {
                        isDownloading = true
                        coroutineScope.launch {
                            val apkFile = otaRepository.downloadApk(releaseInfo!!.downloadUrl) { progress, current, total ->
                                downloadProgress = progress
                                val mbCurrent = current / (1024f * 1024f)
                                val mbTotal = total / (1024f * 1024f)
                                downloadText = String.format("%.1f MB / %.1f MB (%.0f%%)", mbCurrent, mbTotal, progress * 100)
                            }
                            isDownloading = false
                            if (apkFile != null) {
                                otaRepository.installApk(apkFile)
                                onDismiss()
                            } else {
                                statusMessage = "Error al descargar el APK."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlashGreen, contentColor = BgDark)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Descargar e Instalar", fontWeight = FontWeight.Bold)
                }
            } else if (!isDownloading) {
                Button(
                    onClick = {
                        isChecking = true
                        statusMessage = null
                        coroutineScope.launch {
                            val info = otaRepository.checkLatestRelease(currentVersion)
                            isChecking = false
                            if (info != null) {
                                releaseInfo = info
                                if (!info.isNewer) {
                                    statusMessage = "Ya tienes la última versión instalada (${info.tagName})."
                                }
                            } else {
                                statusMessage = "No se pudo conectar a GitHub o no hay releases."
                            }
                        }
                    },
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = FlashCyan, contentColor = BgDark)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BgDark, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscando...")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscar Actualizaciones")
                    }
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = TextSecondary)
                }
            }
        }
    )
}
