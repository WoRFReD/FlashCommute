// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flashchip.flashcommute.data.preferences.SettingsManager
import com.flashchip.flashcommute.ui.screens.*
import com.flashchip.flashcommute.ui.theme.BgDark
import com.flashchip.flashcommute.ui.theme.FlashCommuteTheme

enum class Screen {
    HOME,
    SETTINGS,
    API_GUIDE
}

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)

        setContent {
            FlashCommuteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.HOME) }
                    var showAboutDialog by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
                        PermissionsBanner()

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentScreen) {
                                Screen.HOME -> {
                                    HomeScreen(
                                        settingsManager = settingsManager,
                                        onOpenSettings = { currentScreen = Screen.SETTINGS },
                                        onOpenGuide = { currentScreen = Screen.API_GUIDE },
                                        onOpenAbout = { showAboutDialog = true }
                                    )
                                }
                                Screen.SETTINGS -> {
                                    SettingsScreen(
                                        settingsManager = settingsManager,
                                        onBack = { currentScreen = Screen.HOME },
                                        onOpenGuide = { currentScreen = Screen.API_GUIDE }
                                    )
                                }
                                Screen.API_GUIDE -> {
                                    ApiGuideScreen(
                                        onBack = { currentScreen = Screen.HOME }
                                    )
                                }
                            }
                        }
                    }

                    if (showAboutDialog) {
                        AboutOtaDialog(
                            currentVersion = "1.00",
                            onDismiss = { showAboutDialog = false }
                        )
                    }
                }
            }
        }
    }
}
