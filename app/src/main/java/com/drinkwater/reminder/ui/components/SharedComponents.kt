package com.drinkwater.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.ui.theme.*

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Blue700,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
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
fun SettingsRow(
    icon: ImageVector,
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
fun PickerDialog(
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
fun TimePickerDialog(
    title: String,
    currentTime: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    minuteStep: Int = 30
) {
    val hours = (0..23).toList()
    val minutes = if (minuteStep == 1) (0..59).toList()
        else (0..59 step minuteStep).toList()
    val parts = currentTime.split(":")
    var selectedHour by remember { mutableIntStateOf(parts[0].toInt()) }
    var selectedMinute by remember { mutableIntStateOf(
        minutes.minByOrNull { kotlin.math.abs(it - parts[1].toInt()) } ?: parts[1].toInt()
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("分", style = MaterialTheme.typography.labelLarge, color = Gray600)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
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
