package com.drinkwater.reminder.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class HourlyPrecip(
    val hour: Int,
    val precip: Double,
    val prob: Int
)

data class WeatherInfo(
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val precipitation: Double,
    val locationName: String,
    val hourlyPrecip: List<HourlyPrecip> = emptyList()
) {
    val weatherDesc: String get() = when (weatherCode) {
        0 -> "晴"
        1, 2, 3 -> "多云"
        45, 48 -> "雾"
        51, 53, 55 -> "小雨"
        61, 63, 65 -> "雨"
        71, 73, 75 -> "雪"
        80, 81, 82 -> "阵雨"
        95, 96, 99 -> "雷暴"
        else -> "阴"
    }

    val clothingAdvice: String get() = when {
        maxTemp < 5 -> "天冷，穿羽绒服/厚外套"
        maxTemp < 12 -> "偏凉，穿毛衣/夹克"
        maxTemp < 20 -> "适中，穿长袖/薄外套"
        maxTemp < 28 -> "温暖，穿短袖即可"
        else -> "炎热，注意防晒"
    }

    val umbrellaAdvice: String get() = when {
        precipitation > 5 -> "有雨，记得带伞！"
        precipitation > 0.5 -> "可能有小雨，建议带伞"
        weatherCode in 51..99 -> "有降水概率，备伞"
        else -> "今天无雨，不用带伞"
    }
}

object WeatherHelper {

    private fun getLocation(context: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return null
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            var best: Location? = null
            for (p in providers) {
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (best == null || loc.time > best.time) best = loc
            }
            best?.let { Pair(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    private fun geocode(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.CHINESE)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                // District or locality level
                a.subLocality ?: a.locality ?: a.subAdminArea ?: a.adminArea ?: "当前位置"
            } else "当前位置"
        } catch (e: Exception) {
            "当前位置"
        }
    }

    suspend fun fetchWeather(context: Context): WeatherInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // Get location
                val loc = getLocation(context)
                val lat: Double
                val lon: Double
                val locationName: String

                if (loc != null) {
                    lat = loc.first
                    lon = loc.second
                    locationName = geocode(context, lat, lon)
                } else {
                    // Fallback to Beijing
                    lat = 39.91
                    lon = 116.40
                    locationName = "北京"
                }

                // Fetch daily + hourly weather
                val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode" +
                    "&hourly=precipitation_probability,precipitation" +
                    "&timezone=Asia%2FShanghai&forecast_days=1"

                val conn = URL(urlStr).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (conn.responseCode != 200) {
                    conn.disconnect()
                    return@withContext null
                }

                val json = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val root = JSONObject(json)
                val daily = root.getJSONObject("daily")
                val hourly = root.optJSONObject("hourly")

                val hourlyPrecip = mutableListOf<HourlyPrecip>()
                if (hourly != null) {
                    val times = hourly.getJSONArray("time")
                    val precips = hourly.getJSONArray("precipitation")
                    val probs = hourly.optJSONArray("precipitation_probability")
                    for (i in 0 until times.length()) {
                        val timeStr = times.getString(i)
                        val hour = timeStr.substring(11, 13).toInt()
                        hourlyPrecip.add(HourlyPrecip(
                            hour = hour,
                            precip = precips.getDouble(i),
                            prob = if (probs != null) probs.optInt(i, 0) else 0
                        ))
                    }
                }

                WeatherInfo(
                    maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0),
                    minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0),
                    weatherCode = daily.getJSONArray("weathercode").getInt(0),
                    precipitation = daily.getJSONArray("precipitation_sum").getDouble(0),
                    locationName = locationName,
                    hourlyPrecip = hourlyPrecip
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
