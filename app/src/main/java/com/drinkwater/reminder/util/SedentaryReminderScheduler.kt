package com.drinkwater.reminder.util

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SedentaryReminderScheduler {

    private const val WORK_NAME = "sedentary_reminder_periodic"

    fun schedule(context: Context) {
        val prefs = com.drinkwater.reminder.data.PreferencesManager(context)
        if (!prefs.isSedentaryModuleEnabled()) {
            cancel(context)
            return
        }
        val interval = prefs.getSedentaryInterval().toLong()

        val request = PeriodicWorkRequestBuilder<SedentaryReminderWorker>(
            interval, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).build()

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
