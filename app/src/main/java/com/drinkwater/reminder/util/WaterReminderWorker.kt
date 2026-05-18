package com.drinkwater.reminder.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.drinkwater.reminder.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        val intervalMinutes = prefs.getReminderIntervalMinutes()

        if (!isInQuietHours(prefs)) {
            NotificationHelper.sendWaterReminder(applicationContext, intervalMinutes)
        }

        // Schedule daily summary at 21:00
        scheduleDailySummaryIfNeeded()

        return Result.success()
    }

    private fun isInQuietHours(prefs: PreferencesManager): Boolean {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val quietStart = parseTimeToMinutes(prefs.getQuietStart())
        val quietEnd = parseTimeToMinutes(prefs.getQuietEnd())

        return if (quietStart > quietEnd) {
            // Overnight: e.g. 22:00 - 08:00
            currentMinutes >= quietStart || currentMinutes < quietEnd
        } else {
            currentMinutes in quietStart until quietEnd
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun scheduleDailySummaryIfNeeded() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        // Fire summary around 21:00 (allow some variance from WorkManager scheduling)
        if (hour == 21 && minute in 0..14) {
            val prefs = PreferencesManager(applicationContext)
            val count = prefs.getTodayDrinkCount()
            val goal = prefs.getDailyGoal()
            NotificationHelper.sendDailySummary(applicationContext, count, goal)
        }
    }
}
