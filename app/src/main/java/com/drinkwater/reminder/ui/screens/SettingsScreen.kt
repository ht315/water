package com.drinkwater.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.components.*
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.WaterReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, onNavigateToHelp: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var interval by remember { mutableIntStateOf(prefs.getReminderIntervalMinutes()) }
    var dailyGoal by remember { mutableIntStateOf(prefs.getDailyGoal()) }
    var quietStart by remember { mutableStateOf(prefs.getQuietStart()) }
    var quietEnd by remember { mutableStateOf(prefs.getQuietEnd()) }
    var vibrate by remember { mutableStateOf(prefs.isVibrateEnabled()) }
    var bandVibrate by remember { mutableStateOf(prefs.isBandVibrateEnabled()) }

    var showIntervalPicker by remember { mutableStateOf(false) }
    var showGoalPicker by remember { mutableStateOf(false) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }
    var showBandGuide by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = White
                )
            )
        },
        containerColor = Gray50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle("提醒设置")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Timer,
                    title = "提醒间隔",
                    subtitle = "每 ${interval} 分钟提醒一次",
                    onClick = { showIntervalPicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    title = "震动提醒",
                    subtitle = if (vibrate) "已开启" else "已关闭",
                    onClick = { },
                    trailing = {
                        Switch(
                            checked = vibrate,
                            onCheckedChange = {
                                vibrate = it
                                prefs.setVibrateEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
            }

            SectionTitle("设备联动")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Watch,
                    title = "手环震动",
                    subtitle = if (bandVibrate) "已开启（更强震动模式）" else "已关闭",
                    onClick = { },
                    trailing = {
                        Switch(
                            checked = bandVibrate,
                            onCheckedChange = {
                                bandVibrate = it
                                prefs.setBandVibrateEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "手环通知同步",
                    subtitle = "设置手环接收通知的方法",
                    onClick = { showBandGuide = true }
                )
            }

            SectionTitle("喝水目标")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Flag,
                    title = "每日目标",
                    subtitle = "每天喝 ${dailyGoal} 杯水",
                    onClick = { showGoalPicker = true }
                )
            }

            SectionTitle("其他")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Help,
                    title = "使用帮助",
                    subtitle = "功能介绍、权限说明、厂商适配指南",
                    onClick = onNavigateToHelp
                )
            }

            SectionTitle("免打扰时段")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Bedtime,
                    title = "开始时间",
                    subtitle = quietStart,
                    onClick = { showQuietStartPicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.WbSunny,
                    title = "结束时间",
                    subtitle = quietEnd,
                    onClick = { showQuietEndPicker = true }
                )
            }
        }

        // --- Dialogs ---

        if (showIntervalPicker) {
            val options = listOf(15, 30, 45, 60, 90, 120)
            PickerDialog(
                title = "提醒间隔",
                options = options.map { "${it}分钟" },
                selectedIndex = options.indexOf(interval).coerceAtLeast(0),
                onDismiss = { showIntervalPicker = false },
                onSelect = {
                    interval = options[it]
                    prefs.setReminderIntervalMinutes(interval)
                    WaterReminderScheduler.schedule(context, interval)
                    showIntervalPicker = false
                }
            )
        }

        if (showGoalPicker) {
            val options = (4..20).toList()
            PickerDialog(
                title = "每日目标",
                options = options.map { "${it}杯" },
                selectedIndex = options.indexOf(dailyGoal).coerceAtLeast(0),
                onDismiss = { showGoalPicker = false },
                onSelect = {
                    dailyGoal = options[it]
                    prefs.setDailyGoal(dailyGoal)
                    showGoalPicker = false
                }
            )
        }

        if (showQuietStartPicker) {
            TimePickerDialog(
                title = "免打扰开始",
                currentTime = quietStart,
                onDismiss = { showQuietStartPicker = false },
                onSelect = {
                    quietStart = it
                    prefs.setQuietStart(it)
                    showQuietStartPicker = false
                }
            )
        }

        if (showQuietEndPicker) {
            TimePickerDialog(
                title = "免打扰结束",
                currentTime = quietEnd,
                onDismiss = { showQuietEndPicker = false },
                onSelect = {
                    quietEnd = it
                    prefs.setQuietEnd(it)
                    showQuietEndPicker = false
                }
            )
        }

        if (showBandGuide) {
            AlertDialog(
                onDismissRequest = { showBandGuide = false },
                title = { Text("手环通知同步设置") },
                text = {
                    Column {
                        Text("1. 打开手环对应的运动健康 App", fontSize = 14.sp)
                        Text("2. 进入「设备」页面，选择手环", fontSize = 14.sp)
                        Text("3. 找到「消息通知」或「通知管理」", fontSize = 14.sp)
                        Text("4. 开启「喝水提醒」的通知权限", fontSize = 14.sp)
                        Text("5. 确保手环「通知提醒」已开启", fontSize = 14.sp)
                        Text("6. 收到通知时手环会自动同步震动", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "提示：建议同时开启「手环震动」选项，会使用更强的震动模式以便手环感知。",
                            color = Orange500,
                            fontSize = 13.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBandGuide = false }) {
                        Text("知道了")
                    }
                }
            )
        }
    }
}
