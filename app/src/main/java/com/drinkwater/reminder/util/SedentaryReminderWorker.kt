package com.drinkwater.reminder.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.drinkwater.reminder.data.PreferencesManager
import java.util.Calendar

class SedentaryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val start = parseTimeToMinutes(prefs.getSedentaryStart())
        val end = parseTimeToMinutes(prefs.getSedentaryEnd())

        if (currentMinutes in start until end) {
            val interval = prefs.getSedentaryInterval()
            val vibrate = prefs.isSedentaryVibrateEnabled()
            NotificationHelper.sendSedentaryReminder(applicationContext, interval, vibrate)
        }

        return Result.success()
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}
