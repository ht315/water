package com.drinkwater.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.NotificationHelper

class AttendanceActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        val shift = prefs.getTodayShift()
        if (shift.isEmpty() || shift == "rest") return

        val label = when (shift) {
            "morning" -> "早班"
            "night" -> "晚班"
            "full" -> "通班"
            else -> return
        }
        val startTime = when (shift) {
            "morning" -> prefs.getAttendanceMorningTime()
            "night" -> prefs.getAttendanceNightTime()
            "full" -> prefs.getAttendanceFullTime()
            else -> ""
        }
        val endTime = when (shift) {
            "morning" -> prefs.getAttendanceMorningEndTime()
            "night" -> prefs.getAttendanceNightEndTime()
            "full" -> prefs.getAttendanceFullEndTime()
            else -> ""
        }

        var startDone = prefs.isAttendanceStartDone()
        var endDone = prefs.isAttendanceEndDone()

        when (intent.action) {
            "com.drinkwater.ACTION_START_DONE" -> {
                startDone = true
                prefs.setAttendanceStartDone(true)
                AttendanceAlarmReceiver.cancelSnooze(context)
            }
            "com.drinkwater.ACTION_END_DONE" -> {
                endDone = true
                prefs.setAttendanceEndDone(true)
                AttendanceAlarmReceiver.cancelSnooze(context)
            }
        }

        if (startDone && endDone) {
            NotificationHelper.cancelAttendanceStatus(context)
        } else {
            NotificationHelper.showAttendanceStatus(context, label, startTime, endTime, startDone, endDone)
        }
    }
}
