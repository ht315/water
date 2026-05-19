package com.drinkwater.reminder.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.AttendanceReminderScheduler
import com.drinkwater.reminder.util.WeatherHelper
import com.drinkwater.reminder.util.WeatherInfo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShiftSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show even on lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val scope = rememberCoroutineScope()
            val prefs = remember { PreferencesManager(this@ShiftSelectionActivity) }
            var weather by remember { mutableStateOf<WeatherInfo?>(null) }
            var weatherLoading by remember { mutableStateOf(true) }
            var selected by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                weather = WeatherHelper.fetchWeather(prefs)
                weatherLoading = false
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            "选择今天的班次",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            SimpleDateFormat("MM月dd日 EEEE", Locale.CHINESE).format(Date()),
                            fontSize = 13.sp,
                            color = Gray600
                        )

                        // Weather card
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Blue50)
                        ) {
                            if (weatherLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Blue700, modifier = Modifier.size(24.dp))
                                }
                            } else if (weather != null) {
                                val w = weather!!
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text("${w.cityName}", fontSize = 15.sp, color = Gray700)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                w.weatherDesc,
                                                fontSize = 13.sp,
                                                color = Blue700,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "${w.minTemp.toInt()}° ~ ${w.maxTemp.toInt()}°",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gray800
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Clothing + Umbrella
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Checkroom, contentDescription = null,
                                                tint = Orange500, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(w.clothingAdvice, fontSize = 12.sp, color = Gray600)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (w.precipitation > 0.5) Icons.Default.Umbrella else Icons.Default.WbSunny,
                                                contentDescription = null,
                                                tint = if (w.precipitation > 0.5) Blue700 else Orange500,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(w.umbrellaAdvice, fontSize = 12.sp, color = Gray600)
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("无法获取天气", fontSize = 13.sp, color = Gray500)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("不选择将不会设置打卡提醒", fontSize = 13.sp, color = Gray500, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Shift buttons
                        ShiftButton("早班", Blue700) { saveAndFinish("morning"); selected = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShiftButton("晚班", Blue700) { saveAndFinish("night"); selected = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShiftButton("通班", Blue700) { saveAndFinish("full"); selected = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShiftButton("休息", Gray600) { saveAndFinish("rest"); selected = true }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { if (!selected) finish() }) {
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
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text(label, fontSize = 17.sp)
        }
    }
}
