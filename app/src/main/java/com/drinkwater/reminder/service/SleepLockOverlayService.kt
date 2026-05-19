package com.drinkwater.reminder.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.drinkwater.reminder.DrinkWaterApp
import com.drinkwater.reminder.MainActivity
import com.drinkwater.reminder.R
import com.drinkwater.reminder.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class SleepLockOverlayService : Service() {

    private var overlayView: View? = null
    private var backPressCount = 0
    private var lastBackPressTime = 0L
    private var graceUntil = 0L
    private var longPressStart = 0L
    private var isPressing = false

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = PreferencesManager(this)
        // Check if still in sleep hours
        if (!prefs.isBedtimeModuleEnabled() || !prefs.isBedtimeLockEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.getBooleanExtra("dismiss", false) == true) {
            graceUntil = System.currentTimeMillis() + 15 * 60 * 1000
            removeOverlay()
            // Re-show after grace period
            Thread {
                Thread.sleep(15 * 60 * 1000)
                if (isInSleepHours(prefs)) {
                    showOverlay()
                } else {
                    stopSelf()
                }
            }.start()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Not directly called for overlay views; handle via dispatchKeyEvent
        return super.onKeyDown(keyCode, event)
    }

    private fun showOverlay() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        // Build a simple full-screen overlay
        val root = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
            // Consume all touch events
            setOnTouchListener { _, _ -> true }
        }

        val clockText = TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            setTextColor(android.graphics.Color.WHITE)
            textSize = 72f
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val titleText = TextView(this).apply {
            text = "该睡觉了！"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val hintText = TextView(this).apply {
            text = "睡眠时间已到，放下手机休息吧"
            setTextColor(android.graphics.Color.parseColor("#99FFFFFF"))
            textSize = 14f
            gravity = Gravity.CENTER
        }

        val unlockButton = Button(this).apply {
            text = "长按 3 秒紧急使用"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#33FFFFFF"))
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressing = true
                        longPressStart = System.currentTimeMillis()
                        text = "继续长按..."
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressing = false
                        if (System.currentTimeMillis() - longPressStart >= 3000) {
                            // Dismiss with grace period
                            removeOverlay()
                            graceUntil = System.currentTimeMillis() + 15 * 60 * 1000
                            val prefs = PreferencesManager(this@SleepLockOverlayService)
                            Thread {
                                Thread.sleep(15 * 60 * 1000)
                                if (isInSleepHours(prefs)) {
                                    showOverlay()
                                } else {
                                    stopSelf()
                                }
                            }.start()
                        } else {
                            text = "长按 3 秒紧急使用"
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        val backHint = TextView(this).apply {
            text = "或连续按返回键 3 次"
            setTextColor(android.graphics.Color.parseColor("#33FFFFFF"))
            textSize = 12f
            gravity = Gravity.CENTER
        }

        val vbox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(clockText, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ))
            addView(titleText, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) })
            addView(hintText, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) })
            addView(unlockButton, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
            ).apply {
                topMargin = dp(48)
                leftMargin = dp(60)
                rightMargin = dp(60)
            })
            addView(backHint, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) })
        }
        root.addView(vbox)

        // Update clock every second
        val handler = android.os.Handler(mainLooper)
        val clockUpdater = object : Runnable {
            override fun run() {
                clockText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                // Check if sleep hours ended
                val prefs = PreferencesManager(this@SleepLockOverlayService)
                if (!isInSleepHours(prefs)) {
                    stopSelf()
                    return
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(clockUpdater)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        // FLAG_NOT_FOCUSABLE lets us intercept back key via the view
        // but blocks touch on other apps. We handle touches ourselves.
        root.isFocusableInTouchMode = true
        root.isFocusable = true

        // Override back key via view
        root.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime > 2000) backPressCount = 0
                lastBackPressTime = now
                backPressCount++
                if (backPressCount >= 3) {
                    removeOverlay()
                    graceUntil = System.currentTimeMillis() + 15 * 60 * 1000
                    val prefs = PreferencesManager(this@SleepLockOverlayService)
                    Thread {
                        Thread.sleep(15 * 60 * 1000)
                        if (isInSleepHours(prefs)) showOverlay() else stopSelf()
                    }.start()
                }
                true
            } else false
        }

        windowManager.addView(root, params)
        overlayView = root
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                wm.removeView(it)
            } catch (e: Exception) {}
            overlayView = null
        }
    }

    private fun isInSleepHours(prefs: PreferencesManager): Boolean {
        val bedtime = prefs.getBedtimeTime()
        val wakeUp = prefs.getBedtimeWakeUpTime()
        val bedMin = parseTime(bedtime)
        val wakeMin = parseTime(wakeUp)
        val now = Calendar.getInstance()
        val curMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        // Over-night: e.g., bed=23:00 wake=07:00 -> true for 23:00-23:59 and 00:00-06:59
        return if (bedMin > wakeMin) {
            curMin >= bedMin || curMin < wakeMin
        } else {
            // If bedtime < wakeup (same day, unusual), only during that range
            curMin in bedMin until wakeMin
        }
    }

    private fun parseTime(time: String): Int {
        val p = time.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun startForegroundNotification() {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, DrinkWaterApp.BEDTIME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("睡眠锁屏运行中")
            .setContentText("起床时间后自动解除")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
        startForeground(5003, n)
    }

    companion object {
        fun start(context: Context) {
            context.startForegroundService(Intent(context, SleepLockOverlayService::class.java))
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, SleepLockOverlayService::class.java))
        }
    }
}
