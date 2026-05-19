package com.drinkwater.reminder.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.screens.ReminderPopupActivity
import com.drinkwater.reminder.util.NotificationHelper
import java.util.Calendar

class AttendanceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shiftLabel = intent.getStringExtra("shift_label") ?: return
        val prefs = PreferencesManager(context)

        val isStart = shiftLabel.contains("上班")
        val isEnd = shiftLabel.contains("下班")

        // Check if already marked as done
        if (isStart && prefs.isAttendanceStartDone()) return
        if (isEnd && prefs.isAttendanceEndDone()) return

        NotificationHelper.sendAttendanceReminder(
            context, shiftLabel, prefs.isAttendanceVibrateEnabled()
        )
        if (prefs.isAttendancePopupEnabled()) {
            ReminderPopupActivity.show(context, "打卡提醒",
                "${shiftLabel}打卡时间到了，请及时打卡！", icon = "attendance")
        }

        // Schedule snooze: remind again in 5 minutes
        val snoozeLabel = if (isStart) "上班" else "下班"
        scheduleSnooze(context, shiftLabel, isStart)

        // Refresh status notification
        refreshStatus(context)
    }

    private fun scheduleSnooze(context: Context, label: String, isStart: Boolean) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val snoozeIntent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
                putExtra("shift_label", label)
                putExtra("snooze", true)
            }
            val pi = PendingIntent.getBroadcast(context,
                if (isStart) 3020 else 3021, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, pi), pi)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshStatus(context: Context) {
        val prefs = PreferencesManager(context)
        val shift = prefs.getTodayShift()
        if (shift.isEmpty() || shift == "rest") return

        val label = when (shift) { "morning" -> "早班"; "night" -> "晚班"; "full" -> "通班"; else -> return }
        val startTime = when (shift) {
            "morning" -> prefs.getAttendanceMorningTime(); "night" -> prefs.getAttendanceNightTime()
            "full" -> prefs.getAttendanceFullTime(); else -> ""
        }
        val endTime = when (shift) {
            "morning" -> prefs.getAttendanceMorningEndTime(); "night" -> prefs.getAttendanceNightEndTime()
            "full" -> prefs.getAttendanceFullEndTime(); else -> ""
        }
        NotificationHelper.showAttendanceStatus(context, label, startTime, endTime,
            prefs.isAttendanceStartDone(), prefs.isAttendanceEndDone())
    }

    companion object {
        fun cancelSnooze(context: Context) {
            try {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                listOf(3020, 3021).forEach { code ->
                    val intent = Intent(context, AttendanceAlarmReceiver::class.java)
                    val pi = PendingIntent.getBroadcast(context, code, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    am.cancel(pi)
                }
            } catch (_: Exception) {}
        }
    }
}
