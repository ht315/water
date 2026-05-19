package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.NotificationHelper
import com.drinkwater.reminder.util.SedentaryReminderScheduler
import java.util.Calendar

class SedentaryReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        if (!prefs.isSedentaryModuleEnabled()) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val start = parseTimeToMinutes(prefs.getSedentaryStart())
        val end = parseTimeToMinutes(prefs.getSedentaryEnd())
        val interval = prefs.getSedentaryInterval()
        val vibrate = prefs.isSedentaryVibrateEnabled()

        if (currentMinutes in start until end) {
            NotificationHelper.sendSedentaryReminder(context, interval, vibrate)
        }

        // Re-schedule next alarm
        SedentaryReminderScheduler.schedule(context)
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}
