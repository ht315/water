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
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.components.*
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.AttendanceReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceConfigScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var morningTime by remember { mutableStateOf(prefs.getAttendanceMorningTime()) }
    var nightTime by remember { mutableStateOf(prefs.getAttendanceNightTime()) }
    var fullTime by remember { mutableStateOf(prefs.getAttendanceFullTime()) }
    var vibrate by remember { mutableStateOf(prefs.isAttendanceVibrateEnabled()) }

    var showMorningPicker by remember { mutableStateOf(false) }
    var showNightPicker by remember { mutableStateOf(false) }
    var showFullPicker by remember { mutableStateOf(false) }

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
            SectionTitle("班次时间设置")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.WbSunny,
                    title = "早班提醒时间",
                    subtitle = morningTime,
                    onClick = { showMorningPicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Nightlight,
                    title = "晚班提醒时间",
                    subtitle = nightTime,
                    onClick = { showNightPicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.CalendarToday,
                    title = "通班提醒时间",
                    subtitle = fullTime,
                    onClick = { showFullPicker = true }
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
                        "开启后，每次解锁手机屏幕时会弹出班次选择弹窗，选择当天班次后自动设置提醒时间。\n\n" +
                        "选择「休息」则当天不提醒。\n" +
                        "未选择时弹窗关闭，下次亮屏继续弹出。",
                        fontSize = 14.sp,
                        color = Blue800.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }

    if (showMorningPicker) {
        TimePickerDialog("早班提醒时间", morningTime, { showMorningPicker = false }, minuteStep = 5) {
            morningTime = it
            prefs.setAttendanceMorningTime(it)
            AttendanceReminderScheduler.scheduleIfNeeded(context)
            showMorningPicker = false
        }
    }
    if (showNightPicker) {
        TimePickerDialog("晚班提醒时间", nightTime, { showNightPicker = false }, minuteStep = 5) {
            nightTime = it
            prefs.setAttendanceNightTime(it)
            AttendanceReminderScheduler.scheduleIfNeeded(context)
            showNightPicker = false
        }
    }
    if (showFullPicker) {
        TimePickerDialog("通班提醒时间", fullTime, { showFullPicker = false }, minuteStep = 5) {
            fullTime = it
            prefs.setAttendanceFullTime(it)
            AttendanceReminderScheduler.scheduleIfNeeded(context)
            showFullPicker = false
        }
    }
}
