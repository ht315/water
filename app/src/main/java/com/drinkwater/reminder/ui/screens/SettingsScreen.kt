package com.drinkwater.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.WaterReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var interval by remember { mutableIntStateOf(prefs.getReminderIntervalMinutes()) }
    var dailyGoal by remember { mutableIntStateOf(prefs.getDailyGoal()) }
    var quietStart by remember { mutableStateOf(prefs.getQuietStart()) }
    var quietEnd by remember { mutableStateOf(prefs.getQuietEnd()) }
    var vibrate by remember { mutableStateOf(prefs.isVibrateEnabled()) }

    var showIntervalPicker by remember { mutableStateOf(false) }
    var showGoalPicker by remember { mutableStateOf(false) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
            // Reminder section
            SectionTitle("提醒设置")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Timer,
                    title = "提醒间隔",
                    subtitle = "每 ${interval} 分钟提醒一次",
                    onClick = { showIntervalPicker = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    title = "震动提醒",
                    subtitle = if (vibrate) "已开启" else "已关闭",
                    onClick = { /* noop */ },
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

            // Goal section
            SectionTitle("喝水目标")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Flag,
                    title = "每日目标",
                    subtitle = "每天喝 ${dailyGoal} 杯水",
                    onClick = { showGoalPicker = true }
                )
            }

            // Quiet hours section
            SectionTitle("免打扰时段")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Bedtime,
                    title = "开始时间",
                    subtitle = quietStart,
                    onClick = { showQuietStartPicker = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Blue700,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Blue700, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
private fun PickerDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) },
                            colors = RadioButtonDefaults.colors(selectedColor = Blue700)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun TimePickerDialog(
    title: String,
    currentTime: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val hours = (0..23).toList()
    val minutes = listOf(0, 30)
    val parts = currentTime.split(":")
    var selectedHour by remember { mutableIntStateOf(parts[0].toInt()) }
    var selectedMinute by remember { mutableIntStateOf(parts[1].toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("时", style = MaterialTheme.typography.labelLarge, color = Gray600)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
                        hours.forEach { h ->
                            TextButton(
                                onClick = { selectedHour = h },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = h.toString().padStart(2, '0'),
                                    fontWeight = if (h == selectedHour) FontWeight.Bold else FontWeight.Normal,
                                    color = if (h == selectedHour) Blue700 else Gray800,
                                    fontSize = if (h == selectedHour) 20.sp else 16.sp
                                )
                            }
                        }
                    }
                }

                Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray600)

                // Minute column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("分", style = MaterialTheme.typography.labelLarge, color = Gray600)
                    Spacer(modifier = Modifier.height(4.dp))
                    minutes.forEach { m ->
                        TextButton(
                            onClick = { selectedMinute = m },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = m.toString().padStart(2, '0'),
                                fontWeight = if (m == selectedMinute) FontWeight.Bold else FontWeight.Normal,
                                color = if (m == selectedMinute) Blue700 else Gray800,
                                fontSize = if (m == selectedMinute) 20.sp else 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSelect("${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}")
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
