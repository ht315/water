package com.drinkwater.reminder.ui.screens

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepLockActivity : ComponentActivity() {

    private var backPressCount = 0
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentTime by remember { mutableStateOf(getCurrentTime()) }
            var longPressProgress by remember { mutableFloatStateOf(0f) }
            var lockDismissed by remember { mutableStateOf(false) }
            var isPressing by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    currentTime = getCurrentTime()
                }
            }

            // Handle long press progression
            LaunchedEffect(isPressing) {
                if (isPressing) {
                    longPressProgress = 0f
                    val startTime = System.currentTimeMillis()
                    while (isPressing && longPressProgress < 1f) {
                        delay(50)
                        val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                        longPressProgress = (elapsed / 3f).coerceAtMost(1f)
                    }
                    if (longPressProgress >= 1f) {
                        lockDismissed = true
                    }
                } else if (longPressProgress < 1f) {
                    longPressProgress = 0f
                }
            }

            if (lockDismissed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentTime, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("宽限期 15 分钟", fontSize = 18.sp, color = White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("快去休息吧，不要熬夜！", fontSize = 16.sp, color = White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = {
                            finish()
                        }) {
                            Text("关闭")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            currentTime,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("该睡觉了！", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "睡眠时间已到，放下手机休息吧",
                            fontSize = 16.sp,
                            color = White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(48.dp))

                        // Emergency unlock - long press 3 seconds
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isPressing = true
                                            tryAwaitRelease()
                                            isPressing = false
                                        }
                                    )
                                }
                                .background(
                                    color = if (longPressProgress > 0f)
                                        Blue700.copy(alpha = 0.3f + longPressProgress * 0.5f)
                                    else White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(horizontal = 32.dp, vertical = 16.dp)
                        ) {
                            Text(
                                if (longPressProgress == 0f) "长按 3 秒紧急使用"
                                else "继续长按... ${(longPressProgress * 3).toInt()}s",
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "或连续按返回键 3 次",
                            fontSize = 12.sp,
                            color = White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime > 2000) {
                backPressCount = 0
            }
            lastBackPressTime = now
            backPressCount++
            if (backPressCount >= 3) {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
