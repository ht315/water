package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.screens.ReminderPopupActivity
import com.drinkwater.reminder.ui.screens.SleepLockActivity
import com.drinkwater.reminder.util.BedtimeReminderScheduler
import com.drinkwater.reminder.util.NotificationHelper

class BedtimeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action") ?: return
        val prefs = PreferencesManager(context)
        val vibrate = prefs.isBedtimeVibrateEnabled()
        val popup = prefs.isBedtimePopupEnabled()

        when (action) {
            "advance" -> {
                NotificationHelper.sendBedtimeReminder(context, vibrate)
                if (popup) ReminderPopupActivity.show(context, "睡前提醒", "还有一会儿就该睡觉了，准备收拾一下吧", icon = "bedtime")
            }
            "bedtime" -> {
                NotificationHelper.sendBedtimeReminder(context, vibrate)
                if (popup) ReminderPopupActivity.show(context, "该睡觉了！", "早点休息，明天精神更好", icon = "bedtime")
                if (prefs.isBedtimeLockEnabled()) {
                    val lockIntent = Intent(context, SleepLockActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(lockIntent)
                }
            }
            "wakeup" -> {
                NotificationHelper.sendWakeUpReminder(context, vibrate)
                if (popup) ReminderPopupActivity.show(context, "早上好！", "新的一天开始了", icon = "bedtime")
            }
        }

        // Re-schedule for tomorrow
        BedtimeReminderScheduler.schedule(context)
    }
}
