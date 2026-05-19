package com.drinkwater.reminder.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.drinkwater.reminder.DrinkWaterApp
import com.drinkwater.reminder.MainActivity
import com.drinkwater.reminder.R
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.receiver.ScreenUnlockReceiver
import com.drinkwater.reminder.ui.screens.ShiftSelectionActivity

class AttendanceWatcherService : Service() {

    private val screenUnlockReceiver = ScreenUnlockReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenUnlockReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if still enabled
        val prefs = PreferencesManager(this)
        if (!prefs.isAttendanceModuleEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DrinkWaterApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("打卡提醒运行中")
            .setContentText("解锁屏幕时自动弹出班次选择")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1002, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            unregisterReceiver(screenUnlockReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
        super.onDestroy()
    }

    companion object {
        fun start(context: android.content.Context) {
            val intent = Intent(context, AttendanceWatcherService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: android.content.Context) {
            val intent = Intent(context, AttendanceWatcherService::class.java)
            context.stopService(intent)
        }
    }
}
