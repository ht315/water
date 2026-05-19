package com.drinkwater.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.receiver.AttendanceAlarmReceiver
import java.util.Calendar

object AttendanceReminderScheduler {

    private const val REQ_ATTENDANCE_START = 3001
    private const val REQ_ATTENDANCE_END = 3002
    private const val REQ_PRE_REMINDER = 3003

    fun scheduleIfNeeded(context: Context) {
        try {
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

            val (startTime, endTime) = when (shift) {
                "morning" -> prefs.getAttendanceMorningTime() to prefs.getAttendanceMorningEndTime()
                "night" -> prefs.getAttendanceNightTime() to prefs.getAttendanceNightEndTime()
                "full" -> prefs.getAttendanceFullTime() to prefs.getAttendanceFullEndTime()
                else -> return
            }
            val shiftLabel = when (shift) {
                "morning" -> "早班"
                "night" -> "晚班"
                "full" -> "通班"
                else -> "打卡"
            }

            scheduleOne(context, startTime, "${shiftLabel}上班", REQ_ATTENDANCE_START)
            scheduleOne(context, endTime, "${shiftLabel}下班", REQ_ATTENDANCE_END)

            // Pre-reminder: 1 minute before start time
            if (prefs.isAttendancePreReminderEnabled()) {
                prefs.setPreCheckActive(false)
                val parts = startTime.split(":")
                val h = parts[0].toInt(); val m = parts[1].toInt()
                val preTime = if (m == 0) "${(h - 1 + 24) % 24}:59" else "${h}:${(m - 1).toString().padStart(2, '0')}"
                scheduleOne(context, preTime, "${shiftLabel}预备", REQ_PRE_REMINDER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAll(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            listOf(REQ_ATTENDANCE_START, REQ_ATTENDANCE_END, REQ_PRE_REMINDER).forEach { req ->
                val intent = Intent(context, AttendanceAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, req, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleOne(context: Context, timeStr: String, label: String, reqCode: Int) {
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            putExtra("shift_label", label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reqCode, intent,
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

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
            pendingIntent
        )
    }
}
