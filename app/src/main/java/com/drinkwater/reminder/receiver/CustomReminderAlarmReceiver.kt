package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.screens.ReminderPopupActivity
import com.drinkwater.reminder.util.CustomReminderScheduler
import com.drinkwater.reminder.util.NotificationHelper

class CustomReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: "自定义提醒"
        val vibrate = intent.getBooleanExtra("vibrate", true)
        val repeat = intent.getStringExtra("repeat") ?: "daily"
        val id = intent.getIntExtra("reminder_id", -1)

        NotificationHelper.sendCustomReminder(context, label, vibrate)
        ReminderPopupActivity.show(context, label, "提醒时间到了", icon = "notifications")

        if (repeat == "once" && id >= 0) {
            val prefs = PreferencesManager(context)
            prefs.setCustomReminderEnabled(id, false)
        }

        CustomReminderScheduler.scheduleAll(context)
    }
}
