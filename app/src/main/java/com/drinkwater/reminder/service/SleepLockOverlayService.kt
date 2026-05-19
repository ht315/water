package com.drinkwater.reminder.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
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
    private var testMode = false

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        testMode = intent?.getBooleanExtra("test_mode", false) ?: false
        val prefs = PreferencesManager(this)
        if (!testMode && (!prefs.isBedtimeModuleEnabled() || !prefs.isBedtimeLockEnabled())) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.getBooleanExtra("dismiss", false) == true) {
            removeOverlay()
            Thread {
                Thread.sleep(15 * 60 * 1000)
                if (isInSleepHours(prefs)) showOverlay() else stopSelf()
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

    private fun showOverlay() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val root = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
            setOnTouchListener { _, _ -> true }
        }

        val clockText = TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            setTextColor(android.graphics.Color.WHITE); textSize = 72f
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val titleText = TextView(this).apply {
            text = "该睡觉了！"; setTextColor(android.graphics.Color.WHITE)
            textSize = 24f; gravity = Gravity.CENTER
        }

        val hintText = TextView(this).apply {
            text = "睡眠时间已到，放下手机休息吧"
            setTextColor(android.graphics.Color.parseColor("#99FFFFFF"))
            textSize = 14f; gravity = Gravity.CENTER
        }

        val unlockButton = Button(this).apply {
            text = "长按 3 秒紧急使用"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#33FFFFFF"))
            var pressStart = 0L
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressStart = System.currentTimeMillis(); text = "继续长按..."; true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (System.currentTimeMillis() - pressStart >= 3000) {
                            removeOverlay()
                            val p = PreferencesManager(this@SleepLockOverlayService)
                            Thread {
                                Thread.sleep(15 * 60 * 1000)
                                if (isInSleepHours(p)) showOverlay() else stopSelf()
                            }.start()
                        } else { text = "长按 3 秒紧急使用" }
                        true
                    }
                    else -> false
                }
            }
        }

        val backHint = TextView(this).apply {
            text = "或连续按返回键 3 次"
            setTextColor(android.graphics.Color.parseColor("#33FFFFFF"))
            textSize = 12f; gravity = Gravity.CENTER
        }

        val vbox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            addView(clockText, LinearLayout.LayoutParams(-1, -2))
            addView(titleText, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
            addView(hintText, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
            addView(unlockButton, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(48); leftMargin = dp(60); rightMargin = dp(60) })
            addView(backHint, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        }
        root.addView(vbox)

        root.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime > 2000) backPressCount = 0
                lastBackPressTime = now; backPressCount++
                if (backPressCount >= 3) {
                    removeOverlay()
                    val p = PreferencesManager(this@SleepLockOverlayService)
                    Thread {
                        Thread.sleep(15 * 60 * 1000)
                        if (isInSleepHours(p)) showOverlay() else stopSelf()
                    }.start()
                }
                true
            } else false
        }

        val handler = android.os.Handler(mainLooper)
        handler.post(object : Runnable {
            override fun run() {
                clockText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val p = PreferencesManager(this@SleepLockOverlayService)
                if (!testMode && !isInSleepHours(p)) { stopSelf(); return }
                handler.postDelayed(this, 1000)
            }
        })

        val params = WindowManager.LayoutParams(
            -1, -1, layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(root, params)
        overlayView = root
    }

    private fun removeOverlay() {
        overlayView?.let {
            try { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
            catch (_: Exception) {}
            overlayView = null
        }
    }

    private fun isInSleepHours(prefs: PreferencesManager): Boolean {
        val bedMin = parseTime(prefs.getBedtimeTime())
        val wakeMin = parseTime(prefs.getBedtimeWakeUpTime())
        val curMin = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
        return if (bedMin > wakeMin) curMin >= bedMin || curMin < wakeMin
        else curMin in bedMin until wakeMin
    }

    private fun parseTime(time: String): Int = time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun startForegroundNotification() {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, DrinkWaterApp.BEDTIME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("睡眠锁屏运行中")
            .setContentText("起床时间后自动解除")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true).setContentIntent(pi).build()
        startForeground(5003, n)
    }

    companion object {
        fun start(context: Context, testMode: Boolean = false) {
            val intent = Intent(context, SleepLockOverlayService::class.java).apply {
                putExtra("test_mode", testMode)
            }
            context.startForegroundService(intent)
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, SleepLockOverlayService::class.java))
        }
    }
}
