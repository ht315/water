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
import com.drinkwater.reminder.util.BedtimeReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedtimeConfigScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var bedtime by remember { mutableStateOf(prefs.getBedtimeTime()) }
    var wakeUp by remember { mutableStateOf(prefs.getBedtimeWakeUpTime()) }
    var advance by remember { mutableIntStateOf(prefs.getBedtimeAdvance()) }
    var lockEnabled by remember { mutableStateOf(prefs.isBedtimeLockEnabled()) }
    var vibrate by remember { mutableStateOf(prefs.isBedtimeVibrateEnabled()) }

    var showBedtimePicker by remember { mutableStateOf(false) }
    var showWakeUpPicker by remember { mutableStateOf(false) }
    var showAdvancePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("睡前提醒设置") },
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
            SectionTitle("时间设置")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Bedtime,
                    title = "睡觉时间",
                    subtitle = bedtime,
                    onClick = { showBedtimePicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.WbSunny,
                    title = "起床时间",
                    subtitle = wakeUp,
                    onClick = { showWakeUpPicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "提前提醒",
                    subtitle = if (advance > 0) "提前 ${advance} 分钟" else "准时提醒",
                    onClick = { showAdvancePicker = true }
                )
            }

            SectionTitle("锁机模式")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "睡前软锁屏",
                    subtitle = if (lockEnabled) "已开启（睡觉时间到自动弹出全屏遮罩）" else "已关闭",
                    onClick = { },
                    trailing = {
                        Switch(
                            checked = lockEnabled,
                            onCheckedChange = {
                                lockEnabled = it
                                prefs.setBedtimeLockEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
            }

            SectionTitle("紧急解除方式")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blue50)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "锁屏遮罩出现后，可通过以下方式紧急解除：\n" +
                        "1. 长按「紧急使用」按钮 3 秒\n" +
                        "2. 连续按返回键 3 次\n" +
                        "3. 从通知栏关闭 App 的前台服务\n\n" +
                        "解除后有 15 分钟宽限期，不会再次锁屏。\n" +
                        "起床时间到后自动停止。",
                        fontSize = 14.sp,
                        color = Blue800.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
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
                                prefs.setBedtimeVibrateEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
            }
        }
    }

    if (showBedtimePicker) {
        TimePickerDialog("睡觉时间", bedtime, { showBedtimePicker = false }, minuteStep = 5) {
            bedtime = it
            prefs.setBedtimeTime(it)
            BedtimeReminderScheduler.schedule(context)
            showBedtimePicker = false
        }
    }
    if (showWakeUpPicker) {
        TimePickerDialog("起床时间", wakeUp, { showWakeUpPicker = false }, minuteStep = 5) {
            wakeUp = it
            prefs.setBedtimeWakeUpTime(it)
            BedtimeReminderScheduler.schedule(context)
            showWakeUpPicker = false
        }
    }
    if (showAdvancePicker) {
        val options = listOf(0, 5, 10, 15, 30)
        PickerDialog(
            title = "提前提醒",
            options = options.map { if (it == 0) "准时提醒" else "${it}分钟前" },
            selectedIndex = options.indexOf(advance).coerceAtLeast(0),
            onDismiss = { showAdvancePicker = false },
            onSelect = {
                advance = options[it]
                prefs.setBedtimeAdvance(advance)
                BedtimeReminderScheduler.schedule(context)
                showAdvancePicker = false
            }
        )
    }
}
