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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.service.FloatingWindowService
import com.drinkwater.reminder.ui.screens.*
import com.drinkwater.reminder.util.PermissionHelper
import com.drinkwater.reminder.util.UpdateHelper
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.AttendanceReminderScheduler
import com.drinkwater.reminder.util.BedtimeReminderScheduler
import com.drinkwater.reminder.util.CustomReminderScheduler
import com.drinkwater.reminder.util.SedentaryReminderScheduler
import com.drinkwater.reminder.util.WaterReminderScheduler

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
    ) { granted ->
        if (!granted) {
            PermissionHelper.requestNotificationWithGuide(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)

        requestNotificationPermission()

        // Schedule water reminder on launch
        WaterReminderScheduler.schedule(this, prefs.getReminderIntervalMinutes())

        // Check for updates
        if (prefs.isAutoUpdateEnabled()) {
            Thread {
                val update = UpdateHelper.checkForUpdate(this)
                update?.let { info ->
                    runOnUiThread {
                        android.app.AlertDialog.Builder(this)
                            .setTitle("发现新版本 ${info.versionName}")
                            .setMessage(info.body.take(200))
                            .setPositiveButton("立即更新") { _, _ ->
                                UpdateHelper.downloadAndInstall(this, info.downloadUrl, "update.apk")
                            }
                            .setNegativeButton("稍后", null)
                            .show()
                    }
                }
            }.start()
        }

        setContent {
            DrinkWaterTheme {
                var showOnboarding by remember { mutableStateOf(!prefs.isOnboardingDone()) }

                if (showOnboarding) {
                    OnboardingScreen(onFinish = { showOnboarding = false })
                } else {
                    MainScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val navController = rememberNavController()
        var floatingEnabled by remember { mutableStateOf(prefs.isFloatingEnabled()) }

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = White) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
                    val items = listOf(
                        BottomNavItem("home", "首页", Icons.Default.Home),
                        BottomNavItem("reminders", "提醒", Icons.Default.Notifications),
                        BottomNavItem("settings", "设置", Icons.Default.Settings)
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Blue700,
                                selectedTextColor = Blue700,
                                indicatorColor = Blue50
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
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

                composable("reminders") {
                    RemindersScreen(
                        onNavigateToAttendanceConfig = { navController.navigate("attendance_config") },
                        onNavigateToSedentaryConfig = { navController.navigate("sedentary_config") },
                        onNavigateToBedtimeConfig = { navController.navigate("bedtime_config") },
                        onNavigateToCustomEdit = { id -> navController.navigate("custom_edit/$id") }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = {
                            floatingEnabled = prefs.isFloatingEnabled()
                            navController.popBackStack()
                        },
                        onNavigateToHelp = { navController.navigate("help") }
                    )
                }

                composable("help") {
                    HelpScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable("attendance_config") {
                    AttendanceConfigScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("sedentary_config") {
                    SedentaryConfigScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("bedtime_config") {
                    BedtimeConfigScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "custom_edit/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id") ?: -1
                    CustomReminderEditScreen(
                        reminderId = id,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

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
