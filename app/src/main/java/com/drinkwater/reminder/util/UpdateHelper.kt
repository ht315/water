package com.drinkwater.reminder.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateHelper {

    private const val RELEASES_URL = "https://api.github.com/repos/ht315/water/releases/latest"

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String,
        val body: String
    )

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(RELEASES_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000; conn.readTimeout = 8000
            if (conn.responseCode != 200) { conn.disconnect(); return@withContext null }

            val json = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val release = JSONObject(json)
            val name = release.getString("tag_name")
            val body = release.optString("body", "")
            val assets = release.getJSONArray("assets")
            if (assets.length() == 0) return@withContext null

            val asset = assets.getJSONObject(0)
            val downloadUrl = asset.getString("browser_download_url")
            val vCode = name.replace(".", "").toIntOrNull() ?: 0

            val currentCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val longCode = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
                if (vCode <= longCode) return@withContext null
            } else {
                if (vCode <= currentCode) return@withContext null
            }

            UpdateInfo(vCode, name, downloadUrl, body)
        } catch (e: Exception) { null }
    }

    fun downloadAndInstall(context: Context, url: String, fileName: String) {
        try {
            val file = File(context.externalCacheDir, fileName)
            file.delete()

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("下载更新")
                .setDescription("日常提醒助手")
                .setDestinationUri(Uri.fromFile(file))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val downloadId = dm.enqueue(request)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        ctx.unregisterReceiver(this)
                        installApk(ctx, file)
                    }
                }
            }
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        } catch (e: Exception) {
            Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installApk(context: Context, file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
