package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.service.FloatingWindowService
import com.drinkwater.reminder.util.WaterReminderScheduler
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = PreferencesManager(context)

        // Restart reminders
        WaterReminderScheduler.schedule(context, prefs.getReminderIntervalMinutes())

        // Restart floating window if it was enabled
        if (prefs.isFloatingEnabled()) {
            val serviceIntent = Intent(context, FloatingWindowService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
