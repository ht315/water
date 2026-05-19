package com.drinkwater.reminder.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.drinkwater.reminder.DrinkWaterApp
import com.drinkwater.reminder.MainActivity
import com.drinkwater.reminder.R

object NotificationHelper {

    private fun defaultVibrate(strong: Boolean): LongArray =
        if (strong) longArrayOf(0, 500, 200, 500, 200, 500)
        else longArrayOf(0, 300, 200, 300)

    private fun buildBase(
        context: Context,
        channelId: String,
        title: String,
        text: String,
        vibrate: Boolean,
        bandVibrate: Boolean
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .apply {
                if (vibrate) {
                    setVibrate(defaultVibrate(bandVibrate))
                }
            }
    }

    private fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun vibrate(context: Context): Boolean {
        val prefs = com.drinkwater.reminder.data.PreferencesManager(context)
        return prefs.isVibrateEnabled()
    }

    private fun bandVibrate(context: Context): Boolean {
        val prefs = com.drinkwater.reminder.data.PreferencesManager(context)
        return prefs.isBandVibrateEnabled()
    }

    private fun notify(context: Context, id: Int, n: android.app.Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, n)
    }

    fun sendWaterReminder(context: Context, intervalMinutes: Int, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val n = buildBase(context, DrinkWaterApp.CHANNEL_ID,
            "该喝水啦！", "已经${intervalMinutes}分钟没喝水了，快喝一杯吧",
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 2001, n)
    }

    fun sendDailySummary(context: Context, count: Int, goal: Int, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val achieved = count >= goal
        val title = if (achieved) "今天喝水目标达成！" else "今日喝水总结"
        val text = "今天喝了${count}杯水" + if (achieved) "，已完成目标！" else "，距离目标${goal}杯还差${goal - count}杯"
        val n = buildBase(context, DrinkWaterApp.CHANNEL_ID, title, text,
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 2002, n)
    }

    fun sendAttendanceReminder(context: Context, shiftLabel: String, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val n = buildBase(context, DrinkWaterApp.ATTENDANCE_CHANNEL_ID,
            "打卡提醒", "${shiftLabel}打卡时间到了，请及时打卡！",
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 3001, n)
    }

    fun sendSedentaryReminder(context: Context, minutes: Int, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val n = buildBase(context, DrinkWaterApp.SEDENTARY_CHANNEL_ID,
            "久坐提醒", "已经坐了${minutes}分钟了，起来活动一下吧！",
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 4001, n)
    }

    fun sendBedtimeReminder(context: Context, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val n = buildBase(context, DrinkWaterApp.BEDTIME_CHANNEL_ID,
            "该睡觉了！", "早点休息，明天精神更好",
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 5001, n)
    }

    fun sendWakeUpReminder(context: Context, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val n = buildBase(context, DrinkWaterApp.BEDTIME_CHANNEL_ID,
            "早上好！", "新的一天开始了，记得喝水哦",
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 5002, n)
    }

    fun showAttendanceStatus(context: Context, label: String, startTime: String, endTime: String,
                              startDone: Boolean, endDone: Boolean) {
        if (!checkPermission(context)) return
        val pi = PendingIntent.getActivity(context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val startIcon = if (startDone) "✅" else "⏳"
        val endIcon = if (endDone) "✅" else "⏳"

        // Start action
        val startIntent = Intent(context, com.drinkwater.reminder.receiver.AttendanceActionReceiver::class.java).apply {
            action = "com.drinkwater.ACTION_START_DONE"
        }
        val startPi = PendingIntent.getBroadcast(context, 3010, startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // End action
        val endIntent = Intent(context, com.drinkwater.reminder.receiver.AttendanceActionReceiver::class.java).apply {
            action = "com.drinkwater.ACTION_END_DONE"
        }
        val endPi = PendingIntent.getBroadcast(context, 3011, endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val n = NotificationCompat.Builder(context, DrinkWaterApp.ATTENDANCE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("今日$label")
            .setContentText("$startIcon 上班 $startTime    $endIcon 下班 $endTime")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setContentIntent(pi)
            .setSilent(true)
            .addAction(0, if (startDone) "上班✅" else "上班打卡", startPi)
            .addAction(0, if (endDone) "下班✅" else "下班打卡", endPi)
            .build()
        notify(context, 3009, n)
    }

    fun cancelAttendanceStatus(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(3009)
    }

    fun sendCustomReminder(context: Context, label: String, vibrateEnabled: Boolean) {
        if (!checkPermission(context)) return
        val n = buildBase(context, DrinkWaterApp.CUSTOM_CHANNEL_ID,
            label, "提醒时间到了",
            vibrateEnabled, bandVibrate(context)).build()
        notify(context, 6001, n)
    }
}
