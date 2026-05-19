package com.drinkwater.reminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build
import com.drinkwater.reminder.receiver.ScreenUnlockReceiver

class DrinkWaterApp : Application() {
    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val ATTENDANCE_CHANNEL_ID = "attendance_reminder_channel"
        const val SEDENTARY_CHANNEL_ID = "sedentary_reminder_channel"
        const val BEDTIME_CHANNEL_ID = "bedtime_reminder_channel"
        const val CUSTOM_CHANNEL_ID = "custom_reminder_channel"
    }

    private val screenUnlockReceiver = ScreenUnlockReceiver()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Register screen unlock listener — works while app process is alive
        registerReceiver(
            screenUnlockReceiver,
            IntentFilter(Intent.ACTION_USER_PRESENT)
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        try { unregisterReceiver(screenUnlockReceiver) } catch (_: Exception) {}
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)

        manager.deleteNotificationChannel(CHANNEL_ID)
        val waterChannel = NotificationChannel(
            CHANNEL_ID,
            "喝水提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "定时提醒您喝水"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
            setShowBadge(true)
        }
        manager.createNotificationChannel(waterChannel)

        manager.deleteNotificationChannel(ATTENDANCE_CHANNEL_ID)
        val attendanceChannel = NotificationChannel(
            ATTENDANCE_CHANNEL_ID,
            "打卡提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "上班打卡定时提醒"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }
        manager.createNotificationChannel(attendanceChannel)

        manager.deleteNotificationChannel(SEDENTARY_CHANNEL_ID)
        val sedentaryChannel = NotificationChannel(
            SEDENTARY_CHANNEL_ID,
            "久坐提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "定时提醒活动身体"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }
        manager.createNotificationChannel(sedentaryChannel)

        manager.deleteNotificationChannel(BEDTIME_CHANNEL_ID)
        val bedtimeChannel = NotificationChannel(
            BEDTIME_CHANNEL_ID,
            "睡前提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "睡前提醒和起床通知"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
        manager.createNotificationChannel(bedtimeChannel)

        manager.deleteNotificationChannel(CUSTOM_CHANNEL_ID)
        val customChannel = NotificationChannel(
            CUSTOM_CHANNEL_ID,
            "自定义提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "用户自定义提醒"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }
        manager.createNotificationChannel(customChannel)
    }
}
