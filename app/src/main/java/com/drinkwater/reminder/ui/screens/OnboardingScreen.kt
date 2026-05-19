package com.drinkwater.reminder.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var page by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.WaterDrop,
            title = "喝水提醒",
            description = "定时提醒喝水，设置每日目标\n浮窗快捷记录，养成健康习惯",
            color = Blue700
        ),
        OnboardingPage(
            icon = Icons.Default.CalendarToday,
            title = "打卡提醒",
            description = "解锁屏幕自动弹出班次选择\n上下班时间准时提醒\n支持早班/晚班/通班",
            color = Green600
        ),
        OnboardingPage(
            icon = Icons.Default.Bedtime,
            title = "睡前锁屏",
            description = "到点全屏遮罩防熬夜\n长按3秒紧急解除\n起床时间自动恢复",
            color = Color(0xFF7C3AED)
        ),
        OnboardingPage(
            icon = Icons.Default.Notifications,
            title = "更多提醒",
            description = "久坐提醒 · 自定义提醒\n手环联动 · 免打扰时段\n底部「提醒」页自由管理",
            color = Orange500
        )
    )

    val current = pages[page]
    val bgColor by animateColorAsState(current.color, label = "bg")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Icon circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                current.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            current.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            current.description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.weight(2f))

        // Page dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == page) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == page) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }

        Button(
            onClick = {
                if (page < pages.size - 1) {
                    page++
                } else {
                    prefs.setOnboardingDone()
                    onFinish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                if (page < pages.size - 1) "下一步" else "开始使用",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = bgColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            prefs.setOnboardingDone()
            onFinish()
        }) {
            Text("跳过", color = Color.White.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)
