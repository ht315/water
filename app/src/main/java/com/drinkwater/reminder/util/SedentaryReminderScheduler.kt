package com.drinkwater.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.receiver.SedentaryReminderAlarmReceiver
import java.util.Calendar

object SedentaryReminderScheduler {

    private const val REQ_SEDENTARY = 4001

    fun schedule(context: Context) {
        try {
            val prefs = PreferencesManager(context)
            if (!prefs.isSedentaryModuleEnabled()) {
                cancel(context)
                return
            }
            val interval = prefs.getSedentaryInterval()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, SedentaryReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQ_SEDENTARY, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, interval)
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
            val intent = Intent(context, SedentaryReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQ_SEDENTARY, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
