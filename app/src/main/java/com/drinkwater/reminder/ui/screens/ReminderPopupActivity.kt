package com.drinkwater.reminder.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*
import kotlinx.coroutines.delay

class ReminderPopupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val title = intent.getStringExtra("title") ?: "提醒"
        val message = intent.getStringExtra("message") ?: ""
        val actionType = intent.getStringExtra("action_type") ?: "dismiss"
        val iconName = intent.getStringExtra("icon") ?: "notifications"

        setContent {
            var visible by remember { mutableStateOf(true) }
            var dismissed by remember { mutableStateOf(false) }

            // Auto dismiss after 15 seconds
            LaunchedEffect(Unit) {
                delay(15_000)
                if (visible) {
                    visible = false
                    delay(300)
                    finish()
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { visible = false; delay(300); finish() },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(top = 48.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon
                            val icon = when (iconName) {
                                "water" -> Icons.Default.WaterDrop
                                "sedentary" -> Icons.Default.Accessibility
                                "bedtime" -> Icons.Default.Bedtime
                                "attendance" -> Icons.Default.CalendarToday
                                else -> Icons.Default.Notifications
                            }
                            val iconColor = when (iconName) {
                                "water" -> Blue700
                                "sedentary" -> Orange500
                                "bedtime" -> Color(0xFF7C3AED)
                                "attendance" -> Green600
                                else -> Blue700
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray800,
                                textAlign = TextAlign.Center
                            )

                            if (message.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    message,
                                    fontSize = 16.sp,
                                    color = Gray600,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (actionType == "drink") {
                                // Record drink button
                                Button(
                                    onClick = {
                                        val prefs = PreferencesManager(this@ReminderPopupActivity)
                                        prefs.recordDrink()
                                        visible = false
                                        delay(300)
                                        finish()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue700)
                                ) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("记录喝水", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            TextButton(onClick = {
                                visible = false
                                delay(300)
                                finish()
                            }) {
                                Text(if (actionType == "drink") "稍后再说" else "知道了", color = Gray500)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun show(context: android.content.Context, title: String, message: String, actionType: String = "dismiss", icon: String = "notifications") {
            val intent = Intent(context, ReminderPopupActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("title", title)
                putExtra("message", message)
                putExtra("action_type", actionType)
                putExtra("icon", icon)
            }
            context.startActivity(intent)
        }
    }
}
