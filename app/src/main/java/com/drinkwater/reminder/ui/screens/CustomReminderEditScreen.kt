package com.drinkwater.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.components.*
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.CustomReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReminderEditScreen(
    reminderId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    val isNew = reminderId < 0

    var actualId by remember {
        mutableIntStateOf(if (isNew) prefs.getCustomReminderCount() else reminderId)
    }

    var label by remember { mutableStateOf(if (isNew) "" else prefs.getCustomReminderLabel(reminderId)) }
    var time by remember { mutableStateOf(if (isNew) "08:00" else prefs.getCustomReminderTime(reminderId)) }
    var repeat by remember { mutableStateOf(if (isNew) "daily" else prefs.getCustomReminderRepeat(reminderId)) }
    var weekdays by remember { mutableStateOf(if (isNew) "1,2,3,4,5" else prefs.getCustomReminderWeekdays(reminderId)) }
    var vibrate by remember { mutableStateOf(if (isNew) true else prefs.isCustomReminderVibrateEnabled(reminderId)) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "新建提醒" else "编辑提醒") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = White)
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { showConfirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = White)
                        }
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
            SectionTitle("提醒内容")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Label, contentDescription = null, tint = Blue700, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("标签名称", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = label,
                            onValueChange = { if (it.length <= 20) label = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("例如：开会、交作业") },
                            singleLine = true
                        )
                    }
                }
            }

            SectionTitle("时间设置")
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Schedule,
                    title = "提醒时间",
                    subtitle = time,
                    onClick = { showTimePicker = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Repeat,
                    title = "重复模式",
                    subtitle = when (repeat) {
                        "once" -> "一次性"
                        "daily" -> "每天"
                        "weekly" -> "每周"
                        else -> repeat
                    },
                    onClick = { showRepeatPicker = true }
                )
                if (repeat == "weekly") {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
                    val selectedDays = remember(weekdays) {
                        weekdays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                    }
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("选择星期", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (i in 1..7) {
                                val day = i
                                val isSelected = day in selectedDays
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val newSet = if (isSelected) selectedDays - day else selectedDays + day
                                        if (newSet.isNotEmpty()) {
                                            weekdays = newSet.sorted().joinToString(",")
                                        }
                                    },
                                    label = { Text(dayLabels[i - 1], textAlign = TextAlign.Center) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Blue700,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                        }
                    }
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
                            onCheckedChange = { vibrate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue700
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isNew) {
                        prefs.setCustomReminderCount(actualId + 1)
                    }
                    prefs.setCustomReminderLabel(actualId, label.ifEmpty { "未命名" })
                    prefs.setCustomReminderTime(actualId, time)
                    prefs.setCustomReminderEnabled(actualId, true)
                    prefs.setCustomReminderRepeat(actualId, repeat)
                    prefs.setCustomReminderWeekdays(actualId, weekdays)
                    prefs.setCustomReminderVibrateEnabled(actualId, vibrate)
                    CustomReminderScheduler.scheduleAll(context)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue700)
            ) {
                Text("保存", fontSize = 18.sp)
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                title = "提醒时间",
                currentTime = time,
                onDismiss = { showTimePicker = false },
                minuteStep = 5,
                onSelect = {
                    time = it
                    showTimePicker = false
                }
            )
        }

        if (showRepeatPicker) {
            val options = listOf("once", "daily", "weekly")
            val labels = listOf("一次性", "每天", "每周")
            PickerDialog(
                title = "重复模式",
                options = labels,
                selectedIndex = options.indexOf(repeat).coerceAtLeast(0),
                onDismiss = { showRepeatPicker = false },
                onSelect = {
                    repeat = options[it]
                    showRepeatPicker = false
                }
            )
        }

        if (showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                title = { Text("删除提醒") },
                text = { Text("确定要删除「${label.ifEmpty { "未命名" }}」吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        prefs.deleteCustomReminder(actualId)
                        CustomReminderScheduler.cancelOne(context, actualId)
                        onNavigateBack()
                    }) {
                        Text("删除", color = Red500)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDelete = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
