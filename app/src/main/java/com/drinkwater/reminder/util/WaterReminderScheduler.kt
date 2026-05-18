package com.drinkwater.reminder.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WaterReminderScheduler {

    private const val WORK_NAME = "water_reminder_periodic"

    fun schedule(context: Context, intervalMinutes: Int) {
        val constraints = Constraints.Builder()
            .build()

        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // flex interval
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
