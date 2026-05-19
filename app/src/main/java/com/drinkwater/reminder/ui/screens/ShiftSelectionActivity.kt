package com.drinkwater.reminder.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.AttendanceReminderScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShiftSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var selected by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "选择今天的班次",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "不选择将不会设置打卡提醒",
                            fontSize = 14.sp,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        ShiftButton("早班", Blue700) {
                            saveAndFinish("morning")
                            selected = true
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        ShiftButton("晚班", Blue700) {
                            saveAndFinish("night")
                            selected = true
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        ShiftButton("通班", Blue700) {
                            saveAndFinish("full")
                            selected = true
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        ShiftButton("休息", Gray600) {
                            saveAndFinish("rest")
                            selected = true
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            if (!selected) finish()
                        }) {
                            Text("暂不选择", color = Gray500)
                        }
                    }
                }
            }
        }
    }

    private fun saveAndFinish(shift: String) {
        val prefs = PreferencesManager(this)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.setTodayShift(shift)
        prefs.setTodayShiftDate(today)
        AttendanceReminderScheduler.scheduleIfNeeded(this)
        finish()
    }

    @Composable
    private fun ShiftButton(label: String, color: Color, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text(label, fontSize = 18.sp)
        }
    }
}
