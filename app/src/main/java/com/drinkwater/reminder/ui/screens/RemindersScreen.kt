package com.drinkwater.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.service.AttendanceWatcherService
import com.drinkwater.reminder.ui.components.SectionTitle
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateToAttendanceConfig: () -> Unit,
    onNavigateToSedentaryConfig: () -> Unit,
    onNavigateToBedtimeConfig: () -> Unit,
    onNavigateToCustomEdit: (Int) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var attendanceEnabled by remember { mutableStateOf(prefs.isAttendanceModuleEnabled()) }
    var sedentaryEnabled by remember { mutableStateOf(prefs.isSedentaryModuleEnabled()) }
    var bedtimeEnabled by remember { mutableStateOf(prefs.isBedtimeModuleEnabled()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCustomEdit(-1) },
                containerColor = Blue700,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        },
        containerColor = Gray50
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { SectionTitle("内置提醒") }

            item {
                ModuleCard(
                    icon = Icons.Default.CalendarToday,
                    title = "打卡提醒",
                    subtitle = "上班打卡定时提醒，亮屏自动弹出班次选择",
                    enabled = attendanceEnabled,
                    onToggle = {
                        attendanceEnabled = it
                        prefs.setAttendanceModuleEnabled(it)
                        if (it) {
                            AttendanceReminderScheduler.scheduleIfNeeded(context)
                            AttendanceWatcherService.start(context)
                        } else {
                            AttendanceReminderScheduler.cancelAll(context)
                            AttendanceWatcherService.stop(context)
                        }
                    },
                    onClick = onNavigateToAttendanceConfig
                )
            }

            item {
                ModuleCard(
                    icon = Icons.Default.Accessibility,
                    title = "久坐提醒",
                    subtitle = "定时提醒活动身体，适合长时间办公或学习",
                    enabled = sedentaryEnabled,
                    onToggle = {
                        sedentaryEnabled = it
                        prefs.setSedentaryModuleEnabled(it)
                        if (it) {
                            SedentaryReminderScheduler.schedule(context)
                        } else {
                            SedentaryReminderScheduler.cancel(context)
                        }
                    },
                    onClick = onNavigateToSedentaryConfig
                )
            }

            item {
                ModuleCard(
                    icon = Icons.Default.Bedtime,
                    title = "睡前提醒",
                    subtitle = "到点提醒睡觉，支持软锁屏防熬夜",
                    enabled = bedtimeEnabled,
                    onToggle = {
                        bedtimeEnabled = it
                        prefs.setBedtimeModuleEnabled(it)
                        if (it) {
                            BedtimeReminderScheduler.schedule(context)
                        } else {
                            BedtimeReminderScheduler.cancel(context)
                        }
                    },
                    onClick = onNavigateToBedtimeConfig
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionTitle("自定义提醒") }

            val customCount = prefs.getCustomReminderCount()
            val customItems = (0 until customCount).toList()
            items(customItems.size) { idx ->
                val id = customItems[idx]
                val label = prefs.getCustomReminderLabel(id)
                val time = prefs.getCustomReminderTime(id)
                val enabled = prefs.isCustomReminderEnabled(id)
                val repeat = prefs.getCustomReminderRepeat(id)
                val repeatText = when (repeat) {
                    "once" -> "一次性"
                    "daily" -> "每天"
                    "weekly" -> "每周"
                    else -> repeat
                }

                ModuleCard(
                    icon = Icons.Default.Alarm,
                    title = label.ifEmpty { "未命名" },
                    subtitle = "$time  $repeatText",
                    enabled = enabled,
                    onToggle = {
                        prefs.setCustomReminderEnabled(id, it)
                        CustomReminderScheduler.scheduleAll(context)
                    },
                    onClick = { onNavigateToCustomEdit(id) }
                )
            }

            if (customItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "暂无自定义提醒，点击右下角按钮添加",
                                color = Gray600
                            )
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Blue700, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = Blue700
                )
            )
        }
    }
}
