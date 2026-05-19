package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.NotificationHelper

class AttendanceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shiftLabel = intent.getStringExtra("shift_label") ?: return
        val prefs = PreferencesManager(context)
        NotificationHelper.sendAttendanceReminder(
            context, shiftLabel, prefs.isAttendanceVibrateEnabled()
        )
    }
}
