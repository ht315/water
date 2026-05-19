package com.drinkwater.reminder.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.drinkwater.reminder.DrinkWaterApp
import com.drinkwater.reminder.MainActivity
import com.drinkwater.reminder.R
import com.drinkwater.reminder.data.PreferencesManager
import com.drinkwater.reminder.util.*

class ReminderGuardService : Service() {

    private val handler = android.os.Handler(mainLooper)
    private lateinit var checkRunnable: Runnable

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        startPeriodicCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }

    private fun startPeriodicCheck() {
        checkRunnable = object : Runnable {
            override fun run() {
                val prefs = PreferencesManager(this@ReminderGuardService)
                val ctx = this@ReminderGuardService

                // Re-schedule all enabled modules every 5 minutes
                if (prefs.isAttendanceModuleEnabled()) {
                    AttendanceReminderScheduler.scheduleIfNeeded(ctx)
                }
                if (prefs.isSedentaryModuleEnabled()) {
                    SedentaryReminderScheduler.schedule(ctx)
                }
                if (prefs.isBedtimeModuleEnabled()) {
                    BedtimeReminderScheduler.schedule(ctx)
                }
                CustomReminderScheduler.scheduleAll(ctx)

                // Also refresh water reminder
                WaterReminderScheduler.schedule(ctx, prefs.getReminderIntervalMinutes())

                handler.postDelayed(this, 5 * 60 * 1000)
            }
        }
        handler.post(checkRunnable)
    }

    private fun startForegroundNotification() {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, DrinkWaterApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("提醒助手保护中")
            .setContentText("确保提醒准时触发")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
        startForeground(9001, n)
    }

    companion object {
        fun start(context: Context) {
            context.startForegroundService(Intent(context, ReminderGuardService::class.java))
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, ReminderGuardService::class.java))
        }
    }
}
