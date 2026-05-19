package com.drinkwater.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.components.*
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.AttendanceReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceConfigScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var morningStart by remember { mutableStateOf(prefs.getAttendanceMorningTime()) }
    var morningEnd by remember { mutableStateOf(prefs.getAttendanceMorningEndTime()) }
    var nightStart by remember { mutableStateOf(prefs.getAttendanceNightTime()) }
    var nightEnd by remember { mutableStateOf(prefs.getAttendanceNightEndTime()) }
    var fullStart by remember { mutableStateOf(prefs.getAttendanceFullTime()) }
    var fullEnd by remember { mutableStateOf(prefs.getAttendanceFullEndTime()) }
    var vibrate by remember { mutableStateOf(prefs.isAttendanceVibrateEnabled()) }

    var showPicker by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("打卡提醒设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700, titleContentColor = White
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
            SectionTitle("早班")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Login,
                    title = "上班打卡",
                    subtitle = morningStart,
                    onClick = { showPicker = "morning_start" }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Logout,
                    title = "下班打卡",
                    subtitle = morningEnd,
                    onClick = { showPicker = "morning_end" }
                )
            }

            SectionTitle("晚班")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Login,
                    title = "上班打卡",
                    subtitle = nightStart,
                    onClick = { showPicker = "night_start" }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Logout,
                    title = "下班打卡",
                    subtitle = nightEnd,
                    onClick = { showPicker = "night_end" }
                )
            }

            SectionTitle("通班")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Login,
                    title = "上班打卡",
                    subtitle = fullStart,
                    onClick = { showPicker = "full_start" }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Logout,
                    title = "下班打卡",
                    subtitle = fullEnd,
                    onClick = { showPicker = "full_end" }
                )
            }

            SectionTitle("通知设置")
            SettingsCard {
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
                                prefs.setAttendanceVibrateEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
            }

            SectionTitle("使用说明")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blue50)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "开启后，每次解锁手机屏幕时会弹出班次选择弹窗，选择当天班次后自动设置上下班打卡提醒。\n\n" +
                        "每个班次可分别设置上班和下班两个提醒时间。\n" +
                        "选择「休息」则当天不提醒。",
                        fontSize = 14.sp,
                        color = Blue800.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }

    if (showPicker.isNotEmpty()) {
        val pickerConfig = when (showPicker) {
            "morning_start" -> Triple("早班上班", morningStart, { t: String -> morningStart = t; prefs.setAttendanceMorningTime(t) })
            "morning_end" -> Triple("早班下班", morningEnd, { t: String -> morningEnd = t; prefs.setAttendanceMorningEndTime(t) })
            "night_start" -> Triple("晚班上班", nightStart, { t: String -> nightStart = t; prefs.setAttendanceNightTime(t) })
            "night_end" -> Triple("晚班下班", nightEnd, { t: String -> nightEnd = t; prefs.setAttendanceNightEndTime(t) })
            "full_start" -> Triple("通班上班", fullStart, { t: String -> fullStart = t; prefs.setAttendanceFullTime(t) })
            "full_end" -> Triple("通班下班", fullEnd, { t: String -> fullEnd = t; prefs.setAttendanceFullEndTime(t) })
            else -> return
        }
        TimePickerDialog(
            title = pickerConfig.first,
            currentTime = pickerConfig.second,
            onDismiss = { showPicker = "" },
            minuteStep = 5,
            onSelect = {
                pickerConfig.third(it)
                AttendanceReminderScheduler.scheduleIfNeeded(context)
                showPicker = ""
            }
        )
    }
}
