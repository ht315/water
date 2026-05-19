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
import com.drinkwater.reminder.util.SedentaryReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedentaryConfigScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var interval by remember { mutableIntStateOf(prefs.getSedentaryInterval()) }
    var startTime by remember { mutableStateOf(prefs.getSedentaryStart()) }
    var endTime by remember { mutableStateOf(prefs.getSedentaryEnd()) }
    var vibrate by remember { mutableStateOf(prefs.isSedentaryVibrateEnabled()) }

    var showIntervalPicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("久坐提醒设置") },
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
                                prefs.setSedentaryVibrateEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
            }

            SectionTitle("生效时段")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.PlayArrow,
                    title = "开始时间",
                    subtitle = startTime,
                    onClick = { showStartPicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Stop,
                    title = "结束时间",
                    subtitle = endTime,
                    onClick = { showEndPicker = true }
                )
            }
        }
    }

    if (showIntervalPicker) {
        val options = listOf(15, 30, 45, 60, 90)
        PickerDialog(
            title = "提醒间隔",
            options = options.map { "${it}分钟" },
            selectedIndex = options.indexOf(interval).coerceAtLeast(0),
            onDismiss = { showIntervalPicker = false },
            onSelect = {
                interval = options[it]
                prefs.setSedentaryInterval(interval)
                SedentaryReminderScheduler.schedule(context)
                showIntervalPicker = false
            }
        )
    }
    if (showStartPicker) {
        TimePickerDialog("开始时间", startTime, { showStartPicker = false }) {
            startTime = it
            prefs.setSedentaryStart(it)
            SedentaryReminderScheduler.schedule(context)
            showStartPicker = false
        }
    }
    if (showEndPicker) {
        TimePickerDialog("结束时间", endTime, { showEndPicker = false }) {
            endTime = it
            prefs.setSedentaryEnd(it)
            SedentaryReminderScheduler.schedule(context)
            showEndPicker = false
        }
    }
}
