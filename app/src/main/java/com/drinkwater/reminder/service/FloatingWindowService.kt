package com.drinkwater.reminder.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.drinkwater.reminder.DrinkWaterApp
import com.drinkwater.reminder.MainActivity
import com.drinkwater.reminder.R
import com.drinkwater.reminder.data.PreferencesManager

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var prefs: PreferencesManager

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = PreferencesManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createFloatingWindow()
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun createFloatingWindow() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_bubble, null)

        val icon = floatingView.findViewById<ImageView>(R.id.bubble_icon)
        val countText = floatingView.findViewById<TextView>(R.id.bubble_count)

        countText.text = prefs.getTodayDrinkCount().toString()

        icon.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(dx) > 15 || kotlin.math.abs(dy) > 15) {
                        isDragging = true
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        prefs.recordDrink()
                        countText.text = prefs.getTodayDrinkCount().toString()

                        icon.animate()
                            .scaleX(1.4f).scaleY(1.4f)
                            .setDuration(120)
                            .setInterpolator(OvershootInterpolator())
                            .withEndAction {
                                icon.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(120)
                                    .start()
                            }
                            .start()

                        val today = prefs.getTodayDrinkCount()
                        Toast.makeText(
                            this,
                            "已喝水！今日：${today}杯",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Snap to edge
                        snapToEdge()
                    }
                    true
                }
                else -> false
            }
        }
        windowManager.addView(floatingView, params)
    }

    private fun snapToEdge() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val viewWidth = floatingView.width

        params.x = if (params.x + viewWidth / 2 < screenWidth / 2) {
            0
        } else {
            screenWidth - viewWidth
        }
        windowManager.updateViewLayout(floatingView, params)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DrinkWaterApp.CHANNEL_ID)
            .setContentTitle("喝水提醒运行中")
            .setContentText("点击浮窗记录喝水")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.setFloatingEnabled(false)
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
