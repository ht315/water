package com.drinkwater.reminder.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.theme.*
import com.drinkwater.reminder.util.AttendanceReminderScheduler
import com.drinkwater.reminder.util.HourlyPrecip
import com.drinkwater.reminder.util.WeatherHelper
import com.drinkwater.reminder.util.WeatherInfo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ShiftSelectionActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            val scope = rememberCoroutineScope()
            val context = this@ShiftSelectionActivity
            var weather by remember { mutableStateOf<WeatherInfo?>(null) }
            var weatherLoading by remember { mutableStateOf(true) }
            var selected by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                weather = WeatherHelper.fetchWeather(context)
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
                        .fillMaxWidth(0.92f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("选择今天的班次", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray800)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            SimpleDateFormat("MM月dd日 EEEE", Locale.CHINESE).format(Date()),
                            fontSize = 13.sp, color = Gray600
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Weather card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Blue50)
                        ) {
                            if (weatherLoading) {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Blue700, modifier = Modifier.size(24.dp))
                                }
                            } else if (weather != null) {
                                val w = weather!!
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(w.locationName, fontSize = 15.sp, color = Gray800)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(w.weatherDesc, fontSize = 13.sp, color = Blue700, fontWeight = FontWeight.Medium)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("${w.minTemp.toInt()}° ~ ${w.maxTemp.toInt()}°", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Gray800)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Checkroom, null, tint = Orange500, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(w.clothingAdvice, fontSize = 12.sp, color = Gray600)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(if (w.precipitation > 0.5) Icons.Default.Umbrella else Icons.Default.WbSunny,
                                                    null, tint = if (w.precipitation > 0.5) Blue700 else Orange500, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(w.umbrellaAdvice, fontSize = 12.sp, color = Gray600)
                                            }
                                        }
                                    }

                                    // Precipitation chart
                                    if (w.hourlyPrecip.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("今日降雨趋势", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray600)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        PrecipChart(w.hourlyPrecip)
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("无法获取天气", fontSize = 13.sp, color = Gray600)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("不选择将不会设置打卡提醒", fontSize = 13.sp, color = Gray600, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(16.dp))

                        ShiftButton("早班", Blue700) { saveAndFinish("morning"); selected = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShiftButton("晚班", Blue700) { saveAndFinish("night"); selected = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShiftButton("通班", Blue700) { saveAndFinish("full"); selected = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShiftButton("休息", Gray600) { saveAndFinish("rest"); selected = true }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { if (!selected) finish() }) {
                            Text("暂不选择", color = Gray600)
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

@Composable
private fun PrecipChart(data: List<HourlyPrecip>) {
    val maxPrecip = data.maxOf { it.precip }.coerceAtLeast(0.5)
    val blueBar = Blue700
    val lightBar = Blue200

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Y-axis labels
            Text("mm", fontSize = 10.sp, color = Gray600)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Chart bars
                Row(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEach { item ->
                        val barFraction = (item.precip / maxPrecip).coerceIn(0.0, 1.0)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Prob label
                            if (item.prob > 30) {
                                Text(
                                    "${item.prob}%",
                                    fontSize = 9.sp,
                                    color = Blue700,
                                    maxLines = 1
                                )
                            }
                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height((72.dp * barFraction.toFloat()).coerceAtLeast(2.dp))
                                    .background(
                                        if (item.precip > 0) Blue700 else Blue200,
                                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                        }
                    }
                }
                // Hour labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    data.forEach { item ->
                        Text(
                            if (item.hour % 3 == 0) "${item.hour}时" else "",
                            fontSize = 9.sp,
                            color = Gray600,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
