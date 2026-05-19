package com.drinkwater.reminder.util

import com.drinkwater.reminder.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherInfo(
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val precipitation: Double,
    val cityName: String
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

    // City name → coordinates mapping for common Chinese cities
    private val cityCoords = mapOf(
        "北京" to Pair(39.91, 116.40),
        "上海" to Pair(31.23, 121.47),
        "广州" to Pair(23.13, 113.26),
        "深圳" to Pair(22.55, 114.10),
        "杭州" to Pair(30.27, 120.15),
        "南京" to Pair(32.06, 118.80),
        "武汉" to Pair(30.58, 114.30),
        "成都" to Pair(30.57, 104.07),
        "重庆" to Pair(29.57, 106.55),
        "西安" to Pair(34.26, 108.94),
        "天津" to Pair(39.13, 117.18),
        "苏州" to Pair(31.30, 120.62),
        "长沙" to Pair(28.23, 112.94),
        "郑州" to Pair(34.76, 113.65),
        "济南" to Pair(36.65, 117.00),
        "青岛" to Pair(36.07, 120.38),
        "大连" to Pair(38.91, 121.61),
        "厦门" to Pair(24.48, 118.09),
        "福州" to Pair(26.07, 119.30),
        "合肥" to Pair(31.82, 117.23),
        "南昌" to Pair(28.68, 115.86),
        "贵阳" to Pair(26.65, 106.63),
        "昆明" to Pair(25.04, 102.68),
        "南宁" to Pair(22.82, 108.37),
        "沈阳" to Pair(41.80, 123.43),
        "长春" to Pair(43.88, 125.32),
        "哈尔滨" to Pair(45.75, 126.64),
        "石家庄" to Pair(38.04, 114.51),
        "太原" to Pair(37.87, 112.55)
    )

    suspend fun fetchWeather(prefs: PreferencesManager): WeatherInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val city = prefs.getAttendanceCity()
                val coords = cityCoords[city] ?: cityCoords["北京"]!!
                val (lat, lon) = coords

                val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode" +
                    "&timezone=Asia%2FShanghai&forecast_days=1"

                val conn = URL(urlStr).openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val json = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val root = JSONObject(json)
                val daily = root.getJSONObject("daily")

                WeatherInfo(
                    maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0),
                    minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0),
                    weatherCode = daily.getJSONArray("weathercode").getInt(0),
                    precipitation = daily.getJSONArray("precipitation_sum").getDouble(0),
                    cityName = city
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
