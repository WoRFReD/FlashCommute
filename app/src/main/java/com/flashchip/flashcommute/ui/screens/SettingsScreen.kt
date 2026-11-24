// Version 1.01 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flashchip.flashcommute.data.api.TrafficRepository
import com.flashchip.flashcommute.data.location.LocationHelper
import com.flashchip.flashcommute.data.location.LocationProvider
import com.flashchip.flashcommute.data.preferences.SettingsManager
import com.flashchip.flashcommute.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    onOpenGuide: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val trafficRepository = remember { TrafficRepository() }
    val locationProvider = remember { LocationProvider(context) }

    val currentSettings by settingsManager.settingsFlow.collectAsState()

    var workAddress by remember { mutableStateOf(currentSettings.workAddress) }
    var workLat by remember { mutableStateOf(currentSettings.workLatitude.toString()) }
    var workLng by remember { mutableStateOf(currentSettings.workLongitude.toString()) }
    var mapsParseSuccess by remember { mutableStateOf<String?>(null) }
    var isParsingMaps by remember { mutableStateOf(false) }

    var btAddress by remember { mutableStateOf(currentSettings.bluetoothDeviceAddress) }
    var btName by remember { mutableStateOf(currentSettings.bluetoothDeviceName) }

    var startHour by remember { mutableStateOf(currentSettings.startHour) }
    var startMin by remember { mutableStateOf(currentSettings.startMinute) }
    var endHour by remember { mutableStateOf(currentSettings.endHour) }
    var endMin by remember { mutableStateOf(currentSettings.endMinute) }
    var activeDays by remember { mutableStateOf(currentSettings.activeDays) }

    var criticalDelay by remember { mutableStateOf(currentSettings.criticalDelayMinutes.toFloat()) }
    var selectedProvider by remember { mutableStateOf(currentSettings.trafficProvider) }

    var tomtomKey by remember { mutableStateOf(currentSettings.tomtomApiKey) }
    var showTomtomKey by remember { mutableStateOf(false) }
    var tomtomTestStatus by remember { mutableStateOf<Boolean?>(null) }
    var isTestingTomtom by remember { mutableStateOf(false) }

    var googleKey by remember { mutableStateOf(currentSettings.googleApiKey) }
    var showGoogleKey by remember { mutableStateOf(false) }
    var googleTestStatus by remember { mutableStateOf<Boolean?>(null) }
    var isTestingGoogle by remember { mutableStateOf(false) }

    var autoEnabled by remember { mutableStateOf(currentSettings.autoCheckEnabled) }
    var showBtDialog by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    fun saveAll() {
        val lat = workLat.toDoubleOrNull() ?: 40.4168
        val lng = workLng.toDoubleOrNull() ?: -3.7038

        val updated = currentSettings.copy(
            workAddress = workAddress,
            workLatitude = lat,
            workLongitude = lng,
            bluetoothDeviceAddress = btAddress,
            bluetoothDeviceName = btName,
            startHour = startHour,
            startMinute = startMin,
            endHour = endHour,
            endMinute = endMin,
            activeDays = activeDays,
            criticalDelayMinutes = criticalDelay.toInt(),
            trafficProvider = selectedProvider,
            tomtomApiKey = tomtomKey.trim(),
            googleApiKey = googleKey.trim(),
            autoCheckEnabled = autoEnabled
        )
        settingsManager.updateSettings(updated)
        showSavedSnackbar = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de FlashCommute", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = FlashCyan)
                    }
                },
                actions = {
                    IconButton(onClick = { saveAll() }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar", tint = FlashGreen)
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

            if (showSavedSnackbar) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FlashGreen.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, FlashGreen, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FlashGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajustes guardados correctamente", color = FlashGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // 1. DISPOSITIVO BLUETOOTH DEL COCHE
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = FlashCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bluetooth del Coche", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (btName.isNotBlank()) "🚗 Dispositivo: $btName ($btAddress)" else "🚗 Activar con cualquier Bluetooth del coche (o selecciona uno específico)",
                        fontSize = 13.sp,
                        color = if (btName.isNotBlank()) FlashCyan else TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showBtDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = FlashCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elegir Dispositivo Emparejado", fontSize = 12.sp)
                        }
                        if (btAddress.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    btAddress = ""
                                    btName = ""
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Limpiar", color = FlashRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. DESTINO DE TRABAJO (CON GOOGLE MAPS DIRECTO)
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = FlashAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Destino de Trabajo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Puedes abrir Google Maps, copiar el enlace de tu trabajo y pegarlo aquí con un toque:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Botones de Google Maps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = FlashAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isParsingMaps = true
                                mapsParseSuccess = null
                                coroutineScope.launch {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                    val parsed = LocationHelper.parseMapsInput(context, clipText)
                                    isParsingMaps = false
                                    if (parsed != null) {
                                        workLat = String.format(java.util.Locale.US, "%.6f", parsed.latitude)
                                        workLng = String.format(java.util.Locale.US, "%.6f", parsed.longitude)
                                        workAddress = parsed.addressName
                                        mapsParseSuccess = "Ubicación fijada: ${parsed.addressName}"
                                    } else {
                                        mapsParseSuccess = "Copia primero el enlace o dirección en Google Maps y vuelve a pulsar aquí."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlashAmber, contentColor = BgDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isParsingMaps) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = BgDark, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pegar Enlace Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (mapsParseSuccess != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mapsParseSuccess!!,
                            color = if (mapsParseSuccess!!.startsWith("Ubicación")) FlashGreen else FlashAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = workAddress,
                        onValueChange = { workAddress = it },
                        label = { Text("Nombre / Dirección del Trabajo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlashCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = workLat,
                            onValueChange = { workLat = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlashCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = workLng,
                            onValueChange = { workLng = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlashCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val loc = locationProvider.getCurrentLocation()
                                if (loc != null) {
                                    workLat = String.format(java.util.Locale.US, "%.6f", loc.latitude)
                                    workLng = String.format(java.util.Locale.US, "%.6f", loc.longitude)
                                    mapsParseSuccess = "Coordenadas fijadas con tu GPS actual."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = FlashAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Usar mi ubicación GPS actual", fontSize = 12.sp)
                    }
                }
            }

            // 3. HORARIO Y DÍAS ACTIVOS
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = FlashGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ventana Horaria de Salida", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Franja activa: %02d:%02d a %02d:%02d".format(startHour, startMin, endHour, endMin),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FlashGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = "%02d:%02d".format(startHour, startMin),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora Inicio") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { startHour = (startHour + 1) % 24 }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "+1h", tint = FlashCyan)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlashCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = "%02d:%02d".format(endHour, endMin),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora Fin") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { endHour = (endHour + 1) % 24 }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "+1h", tint = FlashCyan)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlashCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Días de activación:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    val days = listOf(
                        Calendar.MONDAY to "L",
                        Calendar.TUESDAY to "M",
                        Calendar.WEDNESDAY to "X",
                        Calendar.THURSDAY to "J",
                        Calendar.FRIDAY to "V",
                        Calendar.SATURDAY to "S",
                        Calendar.SUNDAY to "D"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { (dayId, label) ->
                            val isSelected = activeDays.contains(dayId)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FlashGreen else SurfaceCard,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable {
                                        activeDays = if (isSelected) activeDays - dayId else activeDays + dayId
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) BgDark else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. UMBRAL DE ALERTA CRÍTICA
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = FlashRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Umbral de Alerta Crítica", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(
                            text = "+${criticalDelay.toInt()} min",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlashRed
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Si el tráfico provoca un retraso de ${criticalDelay.toInt()} min o más, la app disparará una alerta prioritaria en tu móvil.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = criticalDelay,
                        onValueChange = { criticalDelay = it },
                        valueRange = 3f..25f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = FlashRed,
                            activeTrackColor = FlashRed,
                            inactiveTrackColor = SurfaceCard
                        )
                    )
                }
            }

            // 5. MOTOR DE TRÁFICO Y CLAVES API (BOTONES GRANDES Y MISMO COLOR)
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = FlashCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Motor de Tráfico", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        TextButton(onClick = onOpenGuide) {
                            Text("📘 Tutorial APIs", color = FlashCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de Modo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "HYBRID" to "Híbrido (Auto)",
                            "TOMTOM" to "TomTom",
                            "GOOGLE" to "Google"
                        ).forEach { (id, label) ->
                            val isSelected = selectedProvider == id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FlashCyan else SurfaceCard,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedProvider = id }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) BgDark else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ---------------- CLAVE TOMTOM ----------------
                    Text("1. API TOMTOM (2.500/DÍA GRATIS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FlashCyan, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tomtomKey,
                        onValueChange = {
                            tomtomKey = it
                            tomtomTestStatus = null
                        },
                        label = { Text("Pega aquí tu clave API de TomTom") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showTomtomKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showTomtomKey = !showTomtomKey }) {
                                Icon(
                                    if (showTomtomKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlashCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Grande Probar TomTom
                    val isTomtomKeyFilled = tomtomKey.trim().isNotBlank()
                    Button(
                        onClick = {
                            isTestingTomtom = true
                            tomtomTestStatus = null
                            coroutineScope.launch {
                                tomtomTestStatus = trafficRepository.testTomTomKey(tomtomKey.trim())
                                isTestingTomtom = false
                            }
                        },
                        enabled = isTomtomKeyFilled && !isTestingTomtom,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlashCyan,
                            contentColor = BgDark,
                            disabledContainerColor = SurfaceCard,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        if (isTestingTomtom) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BgDark, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Probando conexión con TomTom...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PROBAR CONEXIÓN TOMTOM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (tomtomTestStatus == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FlashGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().border(1.dp, FlashGreen, RoundedCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FlashGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("✅ ¡Conexión con TomTom exitosa! Clave válida.", color = FlashGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else if (tomtomTestStatus == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FlashRed.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().border(1.dp, FlashRed, RoundedCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = FlashRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("❌ La clave de TomTom no es válida o está incompleta.", color = FlashRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ---------------- CLAVE GOOGLE ROUTES ----------------
                    Text("2. API GOOGLE ROUTES (200$/MES GRATIS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FlashCyan, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = googleKey,
                        onValueChange = {
                            googleKey = it
                            googleTestStatus = null
                        },
                        label = { Text("Pega aquí tu clave API de Google Routes") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showGoogleKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGoogleKey = !showGoogleKey }) {
                                Icon(
                                    if (showGoogleKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlashCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Grande Probar Google (Mismo color FlashCyan)
                    val isGoogleKeyFilled = googleKey.trim().isNotBlank()
                    Button(
                        onClick = {
                            isTestingGoogle = true
                            googleTestStatus = null
                            coroutineScope.launch {
                                googleTestStatus = trafficRepository.testGoogleKey(googleKey.trim())
                                isTestingGoogle = false
                            }
                        },
                        enabled = isGoogleKeyFilled && !isTestingGoogle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlashCyan,
                            contentColor = BgDark,
                            disabledContainerColor = SurfaceCard,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        if (isTestingGoogle) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BgDark, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Probando conexión con Google...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PROBAR CONEXIÓN GOOGLE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (googleTestStatus == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FlashGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().border(1.dp, FlashGreen, RoundedCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FlashGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("✅ ¡Conexión con Google Routes exitosa!", color = FlashGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else if (googleTestStatus == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FlashRed.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().border(1.dp, FlashRed, RoundedCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = FlashRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("❌ La clave de Google Routes no es válida.", color = FlashRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // BOTÓN GUARDAR FINAL
            Button(
                onClick = { saveAll() },
                colors = ButtonDefaults.buttonColors(containerColor = FlashGreen, contentColor = BgDark),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Configuración", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    // Diálogo de selección de Bluetooth emparejado
    if (showBtDialog) {
        @SuppressLint("MissingPermission")
        val pairedDevices = remember {
            try {
                BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        AlertDialog(
            onDismissRequest = { showBtDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Dispositivos Bluetooth Emparejados", color = FlashCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (pairedDevices.isEmpty()) {
                        Text("No se detectaron dispositivos Bluetooth emparejados en este teléfono.", color = TextSecondary, fontSize = 13.sp)
                    } else {
                        pairedDevices.forEach { dev ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        btAddress = dev.address
                                        btName = dev.name ?: "Dispositivo Bluetooth"
                                        showBtDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = FlashCyan)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(dev.name ?: "Desconocido", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(dev.address, color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBtDialog = false }) {
                    Text("Cerrar", color = FlashCyan)
                }
            }
        )
    }
}
