package com.drinkwater.reminder.util

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestNotificationWithGuide(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("需要通知权限")
            .setMessage("提醒功能需要通知权限才能发送提醒。\n\n请前往系统设置开启通知权限。")
            .setPositiveButton("前往设置") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
            .setNegativeButton("稍后再说", null)
            .show()
    }

    fun requestOverlayWithGuide(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("需要浮窗权限")
            .setMessage("浮窗提醒和睡前锁屏需要「显示在其他应用上层」权限。\n\n请前往设置开启。")
            .setPositiveButton("前往设置") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
            .setNegativeButton("稍后再说", null)
            .show()
    }
}
