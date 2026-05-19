package com.drinkwater.reminder.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.NotificationHelper

class WeComNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val prefs = PreferencesManager(this)
        if (!prefs.isAttendanceModuleEnabled() || !prefs.isWeComAutoDetectEnabled()) return

        val packageName = sbn.packageName
        val text = sbn.notification.extras.getString("android.text") ?: return

        // Check WeCom or other work apps
        val workApps = listOf(
            "com.tencent.wework" to listOf("打卡成功", "签到成功"),
            "com.alibaba.android.rimet" to listOf("打卡成功"),
            "com.whaty.qyzhxy" to listOf("打卡成功")
        )

        for ((pkg, keywords) in workApps) {
            if (packageName == pkg) {
                for (kw in keywords) {
                    if (text.contains(kw)) {
                        onWorkCheckDetected(prefs)
                        return
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    private fun onWorkCheckDetected(prefs: PreferencesManager) {
        val shift = prefs.getTodayShift()
        if (shift.isEmpty() || shift == "rest") return

        // Determine if it's start or end time
        val now = java.util.Calendar.getInstance()
        val curMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)

        val startTime = parseTimeToMinutes(when (shift) {
            "morning" -> prefs.getAttendanceMorningTime()
            "night" -> prefs.getAttendanceNightTime()
            "full" -> prefs.getAttendanceFullTime()
            else -> return
        })
        val endTime = parseTimeToMinutes(when (shift) {
            "morning" -> prefs.getAttendanceMorningEndTime()
            "night" -> prefs.getAttendanceNightEndTime()
            "full" -> prefs.getAttendanceFullEndTime()
            else -> return
        })

        val startDone = prefs.isAttendanceStartDone()
        val endDone = prefs.isAttendanceEndDone()

        // Auto-mark: if close to start time, mark start; if close to end time, mark end
        if (!startDone && kotlin.math.abs(curMinutes - startTime) <= 30) {
            prefs.setAttendanceStartDone(true)
            refreshNotification(prefs)
        } else if (!endDone && kotlin.math.abs(curMinutes - endTime) <= 30) {
            prefs.setAttendanceEndDone(true)
            refreshNotification(prefs)
        }
    }

    private fun refreshNotification(prefs: PreferencesManager) {
        val shift = prefs.getTodayShift()
        val label = when (shift) { "morning" -> "早班"; "night" -> "晚班"; "full" -> "通班"; else -> return }
        val startTime = when (shift) {
            "morning" -> prefs.getAttendanceMorningTime(); "night" -> prefs.getAttendanceNightTime()
            "full" -> prefs.getAttendanceFullTime(); else -> ""
        }
        val endTime = when (shift) {
            "morning" -> prefs.getAttendanceMorningEndTime(); "night" -> prefs.getAttendanceNightEndTime()
            "full" -> prefs.getAttendanceFullEndTime(); else -> ""
        }
        val startDone = prefs.isAttendanceStartDone()
        val endDone = prefs.isAttendanceEndDone()
        if (startDone && endDone) {
            NotificationHelper.cancelAttendanceStatus(this)
        } else {
            NotificationHelper.showAttendanceStatus(this, label, startTime, endTime, startDone, endDone)
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val p = time.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    companion object {
        fun isEnabled(context: Context): Boolean {
            val listeners = Settings.Secure.getString(
                context.contentResolver, "enabled_notification_listeners"
            )
            return listeners?.contains(context.packageName) == true
        }

        fun openSettings(context: Context) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
