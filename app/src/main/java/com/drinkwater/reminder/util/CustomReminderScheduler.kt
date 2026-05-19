package com.drinkwater.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.receiver.CustomReminderAlarmReceiver
import java.util.Calendar

object CustomReminderScheduler {

    private const val REQ_BASE = 6000

    fun scheduleAll(context: Context) {
        try {
            val prefs = PreferencesManager(context)
            val count = prefs.getCustomReminderCount()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            for (id in 0 until count) {
                if (!prefs.isCustomReminderEnabled(id)) {
                    cancelOne(context, id)
                    continue
                }
                val time = prefs.getCustomReminderTime(id)
                val repeat = prefs.getCustomReminderRepeat(id)
                val weekdays = prefs.getCustomReminderWeekdays(id)
                val label = prefs.getCustomReminderLabel(id)
                val vibrate = prefs.isCustomReminderVibrateEnabled(id)

                val parts = time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                val intent = Intent(context, CustomReminderAlarmReceiver::class.java).apply {
                    putExtra("reminder_id", id)
                    putExtra("label", label)
                    putExtra("vibrate", vibrate)
                    putExtra("repeat", repeat)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context, REQ_BASE + id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                when (repeat) {
                    "once" -> {
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            prefs.setCustomReminderEnabled(id, false)
                            continue
                        }
                    }
                    "weekly" -> {
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val days = weekdays.split(",").mapNotNull { it.trim().toIntOrNull() }
                        var found = false
                        for (i in 0 until 7) {
                            val checkDay = ((dayOfWeek - 1 + i) % 7) + 1
                            if (checkDay in days) {
                                if (i > 0 || calendar.timeInMillis > System.currentTimeMillis()) {
                                    calendar.add(Calendar.DAY_OF_YEAR, i)
                                    found = true
                                    break
                                }
                            }
                        }
                        if (!found) {
                            calendar.add(Calendar.DAY_OF_YEAR, 7)
                        }
                    }
                    "daily" -> {
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                }

                // setAlarmClock is exempt from SCHEDULE_EXACT_ALARM permission on Android 12+
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelOne(context: Context, id: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CustomReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQ_BASE + id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
