// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flashchip.flashcommute.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiGuideScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de APIs Gratuitas", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = FlashCyan)
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
            // Introducción
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚀 ¿Cómo funcionan las APIs?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlashCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FlashCommute utiliza proveedores oficiales de navegación para calcular el tráfico en tiempo real. Ambas opciones tienen planes gratuitos excelentes que no te costarán nada para tu uso diario personal.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Opción 1: TomTom Developers (Recomendada)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlashGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "1. TomTom Developers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlashGreen
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FlashGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "2.500 pet./día GRATIS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = FlashGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Es la opción más rápida de configurar (no pide tarjeta de crédito obligatoria):",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    StepRow("1", "Accede a la web de TomTom Developers.")
                    StepRow("2", "Haz clic en 'Register' y crea una cuenta gratuita.")
                    StepRow("3", "Ve al panel 'Keys' o 'Dashboard' y copia la clave generada automáticamente.")
                    StepRow("4", "Pégala en Ajustes de FlashCommute y pulsa 'Probar'.")

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.tomtom.com/user/register"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlashGreen, contentColor = BgDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrarse en TomTom Developers", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Opción 2: Google Routes API
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlashCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "2. Google Cloud / Routes API",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlashCyan
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FlashCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "200$/mes GRATIS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = FlashCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Google ofrece una precisión inigualable de tráfico con su bono recurrente de 200 $ mensuales:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    StepRow("1", "Accede a Google Cloud Console con tu cuenta de Google.")
                    StepRow("2", "Crea un proyecto (ej. 'MiFlashCommute').")
                    StepRow("3", "En 'APIs y servicios' busca y activa 'Routes API'.")
                    StepRow("4", "En 'Credenciales', genera una 'Clave de API', cópiala y pégala aquí.")

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.cloud.google.com/apis/library/routes.googleapis.com"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlashCyan, contentColor = BgDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir Google Cloud Console", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tarjeta de Recomendación FlashChip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlashAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FlashAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "💡 Consejo Modo Híbrido",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlashAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Si configuras ambas claves y seleccionas el Modo 'Híbrido', FlashCommute usará TomTom por defecto y, si alguna vez no tiene respuesta o hay saturación, saltará a Google Routes en milisegundos.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(stepNumber: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = SurfaceCard,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stepNumber, color = FlashCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = TextPrimary, fontSize = 13.sp)
    }
}
