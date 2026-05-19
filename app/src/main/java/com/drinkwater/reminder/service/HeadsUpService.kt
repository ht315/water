package com.drinkwater.reminder.service

import android.animation.ObjectAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*

class HeadsUpService : Service() {

    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "提醒"
        val message = intent?.getStringExtra("message") ?: ""
        val iconType = intent?.getStringExtra("icon") ?: "notifications"
        showBanner(title, message, iconType)
        return START_NOT_STICKY
    }

    private fun showBanner(title: String, message: String, iconType: String) {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }

        removeOverlay()
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val density = resources.displayMetrics.density

        // Root container
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(android.graphics.Color.WHITE)
            elevation = 8f * density
            setPadding(dp(14), dp(10), dp(14), dp(10))
            layoutParams = ViewGroup.LayoutParams(-1, -2)

            // Round corners via clip
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, dp(12).toFloat())
                }
            }
            // Shadow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                outlineSpotShadowColor = android.graphics.Color.argb(80, 0, 0, 0)
            }
        }

        // Colored left bar
        val iconColor = when (iconType) {
            "water" -> 0xFF1976D2.toInt()
            "sedentary" -> 0xFFFF9800.toInt()
            else -> 0xFF1976D2.toInt()
        }
        val bar = View(this).apply {
            setBackgroundColor(iconColor)
            layoutParams = LinearLayout.LayoutParams(dp(4), dp(40)).apply {
                rightMargin = dp(12)
            }
        }
        card.addView(bar)

        // Text column
        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        val titleView = TextView(this).apply {
            text = title; textSize = 15f; setTextColor(0xFF212121.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD); maxLines = 1
        }
        val msgView = TextView(this).apply {
            text = message; textSize = 13f; setTextColor(0xFF757575.toInt())
            maxLines = 2
        }
        textCol.addView(titleView)
        if (message.isNotEmpty()) textCol.addView(msgView)
        card.addView(textCol)

        // Close button
        val closeBtn = TextView(this).apply {
            text = "✕"; textSize = 14f; setTextColor(0xFFBDBDBD.toInt())
            setPadding(dp(12), dp(4), 0, dp(4))
            setOnClickListener { dismissAnimated() }
        }
        card.addView(closeBtn)

        // Swipe to dismiss
        card.setOnTouchListener(object : View.OnTouchListener {
            var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> startY = event.rawY
                    MotionEvent.ACTION_UP -> {
                        if (event.rawY - startY < -60) dismissAnimated()
                        else if (event.rawY - startY > 60) dismissAnimated()
                    }
                }
                return true
            }
        })
        card.setOnClickListener { dismissAnimated() }

        overlayView = card

        val params = WindowManager.LayoutParams(
            -1, -2, layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            x = dp(8)
            y = dp(48)
            width = resources.displayMetrics.widthPixels - dp(16)
        }

        wm.addView(card, params)

        // Slide in animation
        card.translationY = -200f
        ObjectAnimator.ofFloat(card, "translationY", -200f, 0f).apply {
            duration = 300; interpolator = DecelerateInterpolator(); start()
        }

        // Auto dismiss after 4 seconds
        dismissRunnable = Runnable { dismissAnimated() }
        handler.postDelayed(dismissRunnable!!, 4000)
    }

    private fun dismissAnimated() {
        val view = overlayView ?: return
        handler.removeCallbacks(dismissRunnable ?: return)
        ObjectAnimator.ofFloat(view, "translationY", view.translationY, -200f).apply {
            duration = 250; interpolator = DecelerateInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    removeOverlay(); stopSelf()
                }
            })
            start()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) } catch (_: Exception) {}
            overlayView = null
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        super.onDestroy()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        fun show(context: Context, title: String, message: String, icon: String = "notifications") {
            val intent = Intent(context, HeadsUpService::class.java).apply {
                putExtra("title", title)
                putExtra("message", message)
                putExtra("icon", icon)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
