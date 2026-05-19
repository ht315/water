package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.service.AttendanceWatcherService
import com.drinkwater.reminder.service.FloatingWindowService
import com.drinkwater.reminder.ui.screens.ShiftSelectionActivity
import com.drinkwater.reminder.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = PreferencesManager(context)

        // Restart water reminder
        WaterReminderScheduler.schedule(context, prefs.getReminderIntervalMinutes())

        // Restart floating window if it was enabled
        if (prefs.isFloatingEnabled()) {
            val serviceIntent = Intent(context, FloatingWindowService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        // Restart attendance
        if (prefs.isAttendanceModuleEnabled()) {
            AttendanceReminderScheduler.scheduleIfNeeded(context)
            AttendanceWatcherService.start(context)
        }

        // Restart sedentary
        if (prefs.isSedentaryModuleEnabled()) {
            SedentaryReminderScheduler.schedule(context)
        }

        // Restart bedtime
        if (prefs.isBedtimeModuleEnabled()) {
            BedtimeReminderScheduler.schedule(context)
        }

        // Restart custom reminders
        CustomReminderScheduler.scheduleAll(context)
    }
}
