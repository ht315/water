package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.ui.screens.ShiftSelectionActivity

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_USER_PRESENT && action != Intent.ACTION_SCREEN_ON) return

        val prefs = PreferencesManager(context)
        if (!prefs.isAttendanceModuleEnabled()) return
        if (prefs.isTodayShiftSelected()) return

        val now = System.currentTimeMillis()
        if (now - lastStartTime < 2000) return
        lastStartTime = now

        val activityIntent = Intent(context, ShiftSelectionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(activityIntent)
    }

    companion object {
        private var lastStartTime = 0L
    }
}
