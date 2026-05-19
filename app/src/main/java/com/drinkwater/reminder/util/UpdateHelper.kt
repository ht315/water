package com.drinkwater.reminder.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
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

    fun checkForUpdate(context: Context): UpdateInfo? {
        return try {
            val conn = URL(RELEASES_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000; conn.readTimeout = 8000
            if (conn.responseCode != 200) { conn.disconnect(); return null }

            val json = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val release = JSONObject(json)
            val name = release.getString("tag_name")
            val body = release.optString("body", "")
            val assets = release.getJSONArray("assets")
            if (assets.length() == 0) return null

            val asset = assets.getJSONObject(0)
            val downloadUrl = asset.getString("browser_download_url")
            val vCode = name.replace(".", "").toIntOrNull() ?: 0

            val currentCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val longCode = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
                if (vCode <= longCode) return null
            } else {
                if (vCode <= currentCode) return null
            }

            UpdateInfo(vCode, name, downloadUrl, body)
        } catch (e: Exception) { null }
    }

    fun downloadAndInstall(context: Context, url: String, fileName: String) {
        Thread {
            try {
                val file = File(context.externalCacheDir, fileName)
                file.delete()

                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 30000; conn.readTimeout = 30000
                conn.setRequestProperty("Accept", "application/octet-stream")
                if (conn.responseCode != 200) {
                    runOnUiThread { Toast.makeText(context, "下载失败: HTTP ${conn.responseCode}", Toast.LENGTH_SHORT).show() }
                    conn.disconnect(); return@Thread
                }

                val total = conn.contentLength
                conn.inputStream.use { input ->
                    file.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var downloaded = 0L
                        var lastProgress = 0L
                        var bytes: Int
                        while (input.read(buffer).also { bytes = it } != -1) {
                            output.write(buffer, 0, bytes)
                            downloaded += bytes
                            if (total > 0 && downloaded - lastProgress > total / 10) {
                                lastProgress = downloaded
                                val pct = (downloaded * 100 / total).toInt()
                                runOnUiThread { Toast.makeText(context, "下载中... $pct%", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                }
                conn.disconnect()

                runOnUiThread { installApk(context, file) }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post(action)
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
