package com.drinkwater.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.receiver.AttendanceAlarmReceiver
import java.util.Calendar

object AttendanceReminderScheduler {

    private const val REQ_ATTENDANCE = 3001

    fun scheduleIfNeeded(context: Context) {
        val prefs = PreferencesManager(context)
        if (!prefs.isAttendanceModuleEnabled()) {
            cancelAll(context)
            return
        }
        val shift = prefs.getTodayShift()
        if (shift.isEmpty() || shift == "rest") {
            cancelAll(context)
            return
        }
        val timeStr = when (shift) {
            "morning" -> prefs.getAttendanceMorningTime()
            "night" -> prefs.getAttendanceNightTime()
            "full" -> prefs.getAttendanceFullTime()
            else -> return
        }
        val shiftLabel = when (shift) {
            "morning" -> "早班"
            "night" -> "晚班"
            "full" -> "通班"
            else -> "打卡"
        }

        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            putExtra("shift_label", shiftLabel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQ_ATTENDANCE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
        )
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AttendanceAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQ_ATTENDANCE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
