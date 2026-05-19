package com.drinkwater.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.receiver.BedtimeAlarmReceiver
import java.util.Calendar

object BedtimeReminderScheduler {

    private const val REQ_BEDTIME_ADVANCE = 5001
    private const val REQ_BEDTIME = 5002
    private const val REQ_WAKEUP = 5003

    fun schedule(context: Context) {
        val prefs = PreferencesManager(context)
        if (!prefs.isBedtimeModuleEnabled()) {
            cancel(context)
            return
        }

        val bedtime = prefs.getBedtimeTime()
        val advance = prefs.getBedtimeAdvance()
        val wakeUp = prefs.getBedtimeWakeUpTime()

        scheduleAlarm(context, bedtime, advance, REQ_BEDTIME_ADVANCE, "advance")
        scheduleAlarm(context, bedtime, 0, REQ_BEDTIME, "bedtime")
        scheduleAlarm(context, wakeUp, 0, REQ_WAKEUP, "wakeup")
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(REQ_BEDTIME_ADVANCE, REQ_BEDTIME, REQ_WAKEUP).forEach { req ->
            val intent = Intent(context, BedtimeAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, req, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun scheduleAlarm(context: Context, timeStr: String, advanceMin: Int, reqCode: Int, action: String) {
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute - advanceMin)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BedtimeAlarmReceiver::class.java).apply {
            putExtra("action", action)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
        )
    }
}
