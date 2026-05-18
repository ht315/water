package com.drinkwater.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.service.FloatingWindowService
import com.drinkwater.reminder.ui.screens.HomeScreen
import com.drinkwater.reminder.ui.screens.SettingsScreen
import com.drinkwater.reminder.ui.theme.DrinkWaterTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            prefs.setFloatingEnabled(true)
            startFloatingService()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 无论是否授权，App 都可继续使用 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)

        requestNotificationPermission()

        setContent {
            DrinkWaterTheme {
                val navController = rememberNavController()
                var floatingEnabled by rememberFloatingState()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            floatingEnabled = floatingEnabled,
                            onToggleFloating = { enable ->
                                if (enable) {
                                    checkAndStartFloating()
                                } else {
                                    stopFloatingService()
                                    prefs.setFloatingEnabled(false)
                                }
                                floatingEnabled = enable
                            },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = {
                                floatingEnabled = prefs.isFloatingEnabled()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun rememberFloatingState() = mutableStateOf(prefs.isFloatingEnabled())

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkAndStartFloating() {
        if (Settings.canDrawOverlays(this)) {
            prefs.setFloatingEnabled(true)
            startFloatingService()
        } else {
            startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopFloatingService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        stopService(intent)
    }
}
