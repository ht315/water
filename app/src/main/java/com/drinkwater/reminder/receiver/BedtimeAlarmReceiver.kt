package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.screens.SleepLockActivity
import com.drinkwater.reminder.util.BedtimeReminderScheduler
import com.drinkwater.reminder.util.NotificationHelper

class BedtimeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action") ?: return
        val prefs = PreferencesManager(context)
        val vibrate = prefs.isBedtimeVibrateEnabled()

        when (action) {
            "advance" -> {
                val advance = prefs.getBedtimeAdvance()
                NotificationHelper.sendBedtimeReminder(context, vibrate)
            }
            "bedtime" -> {
                NotificationHelper.sendBedtimeReminder(context, vibrate)
                if (prefs.isBedtimeLockEnabled()) {
                    val lockIntent = Intent(context, SleepLockActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(lockIntent)
                }
            }
            "wakeup" -> {
                NotificationHelper.sendWakeUpReminder(context, vibrate)
            }
        }

        // Re-schedule for tomorrow
        BedtimeReminderScheduler.schedule(context)
    }
}
