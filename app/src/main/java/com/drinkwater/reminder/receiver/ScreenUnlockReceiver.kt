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

        // Pre-check: 1 min before clock-in, jump directly to WeChat Work
        if (prefs.isAttendancePreReminderEnabled() && prefs.isPreCheckActive()) {
            val shift = prefs.getTodayShift()
            if (shift.isNotEmpty() && shift != "rest" && !prefs.isAttendanceStartDone()) {
                prefs.setPreCheckActive(false)
                // Try to open WeChat Work
                try {
                    val wecomIntent = context.packageManager.getLaunchIntentForPackage("com.tencent.wework")
                    if (wecomIntent != null) {
                        wecomIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(wecomIntent)
                        return
                    }
                } catch (_: Exception) {}
                // Fallback: open DingTalk
                try {
                    val dtIntent = context.packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                    if (dtIntent != null) {
                        dtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(dtIntent)
                        return
                    }
                } catch (_: Exception) {}
            }
            prefs.setPreCheckActive(false)
            return
        }

        // Normal shift selection popup
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
