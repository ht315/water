package com.drinkwater.reminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class DrinkWaterApp : Application() {
    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
    }

    override fun onCreate() {
        super.onCreate()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "喝水提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "定时提醒您喝水" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
