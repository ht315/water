package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.NotificationHelper
import com.drinkwater.reminder.util.WaterReminderScheduler
import java.util.Calendar

class WaterReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        val intervalMinutes = prefs.getReminderIntervalMinutes()
        val vibrateEnabled = prefs.isVibrateEnabled()

        if (!isInQuietHours(prefs)) {
            NotificationHelper.sendWaterReminder(context, intervalMinutes, vibrateEnabled)
        }

        // Schedule daily summary around 21:00
        val now = Calendar.getInstance()
        if (now.get(Calendar.HOUR_OF_DAY) == 21 && now.get(Calendar.MINUTE) in 0..20) {
            val count = prefs.getTodayDrinkCount()
            val goal = prefs.getDailyGoal()
            NotificationHelper.sendDailySummary(context, count, goal, vibrateEnabled)
        }

        // Re-schedule next alarm
        WaterReminderScheduler.schedule(context, intervalMinutes)
    }

    private fun isInQuietHours(prefs: PreferencesManager): Boolean {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val quietStart = parseTimeToMinutes(prefs.getQuietStart())
        val quietEnd = parseTimeToMinutes(prefs.getQuietEnd())
        return if (quietStart > quietEnd) {
            currentMinutes >= quietStart || currentMinutes < quietEnd
        } else {
            currentMinutes in quietStart until quietEnd
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}
