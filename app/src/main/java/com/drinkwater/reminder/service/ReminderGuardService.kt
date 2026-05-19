package com.drinkwater.reminder.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.drinkwater.reminder.MainActivity
import com.drinkwater.reminder.R
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.*

class ReminderGuardService : Service() {

    private val handler = android.os.Handler(mainLooper)
    private var started = false

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!started) {
            startForeground()
            startPeriodicCheck()
            started = true
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel("guard_channel", "后台保护", NotificationManager.IMPORTANCE_MIN).apply {
                description = "确保提醒准时触发"
                setSound(null, null)
                enableVibration(false)
            }
            nm.createNotificationChannel(ch)
        }
    }

    private fun startForeground() {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, "guard_channel")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("提醒助手保护中")
            .setContentText("确保提醒准时触发")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
        startForeground(9001, n)
    }

    private fun startPeriodicCheck() {
        val runnable = object : Runnable {
            override fun run() {
                try {
                    val prefs = PreferencesManager(this@ReminderGuardService)
                    val ctx = this@ReminderGuardService
                    WaterReminderScheduler.schedule(ctx, prefs.getReminderIntervalMinutes())
                    if (prefs.isAttendanceModuleEnabled()) AttendanceReminderScheduler.scheduleIfNeeded(ctx)
                    if (prefs.isSedentaryModuleEnabled()) SedentaryReminderScheduler.schedule(ctx)
                    if (prefs.isBedtimeModuleEnabled()) BedtimeReminderScheduler.schedule(ctx)
                    CustomReminderScheduler.scheduleAll(ctx)
                } catch (_: Exception) {}
                handler.postDelayed(this, 30 * 60 * 1000)
            }
        }
        handler.post(runnable)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ReminderGuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, ReminderGuardService::class.java))
        }
    }
}
