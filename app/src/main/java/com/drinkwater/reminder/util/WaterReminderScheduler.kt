package com.drinkwater.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object WaterReminderScheduler {

    private const val REQ_WATER = 1001

    fun schedule(context: Context, intervalMinutes: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, com.drinkwater.reminder.receiver.WaterReminderAlarmReceiver::class.java)
            intent.putExtra("interval", intervalMinutes)

            val pendingIntent = PendingIntent.getBroadcast(
                context, REQ_WATER, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, intervalMinutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, com.drinkwater.reminder.receiver.WaterReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQ_WATER, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
