package com.drinkwater.reminder.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    floatingEnabled: Boolean,
    onToggleFloating: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var drinkCount by remember { mutableIntStateOf(prefs.getTodayDrinkCount()) }
    val dailyGoal by remember { mutableIntStateOf(prefs.getDailyGoal()) }
    var showRipple by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = (drinkCount.toFloat() / dailyGoal).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            drinkCount = prefs.getTodayDrinkCount()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日常提醒助手") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = White
                        )
                    }
                }
            )
        },
        containerColor = Gray50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress circle
            Card(
                modifier = Modifier.size(240.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        drawArc(
                            color = Blue100,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = if (progress >= 1f) Green500 else Blue700,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$drinkCount",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progress >= 1f) Green600 else Blue700
                        )
                        Text(
                            text = "/ $dailyGoal 杯",
                            fontSize = 16.sp,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (progress >= 1f) "目标达成！" else "${((1 - progress) * 100).toInt()}% 未完成",
                            fontSize = 13.sp,
                            color = if (progress >= 1f) Green500 else Gray600
                        )
                    }
                }
            }

            // Drink button
            Button(
                onClick = {
                    prefs.recordDrink()
                    drinkCount = prefs.getTodayDrinkCount()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue700)
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("记录喝水", fontSize = 18.sp)
            }

            // Floating window toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.BubbleChart,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "浮窗提醒",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "屏幕边缘显示水滴浮窗，点击即可记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                    }
                    Switch(
                        checked = floatingEnabled,
                        onCheckedChange = onToggleFloating,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = Blue700
                        )
                    )
                }
            }

            // Tips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blue50)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "使用提示",
                        style = MaterialTheme.typography.titleMedium,
                        color = Blue800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. 开启浮窗后在屏幕边缘会出现水滴图标\n" +
                                "2. 拖拽浮窗可移动位置，松手自动贴边\n" +
                                "3. 点击浮窗即可快速记录喝水\n" +
                                "4. 进入设置可调整提醒间隔和每日目标",
                        fontSize = 14.sp,
                        color = Blue800.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom spacer for navigation bar
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
