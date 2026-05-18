package com.drinkwater.reminder.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("drink_water_prefs", Context.MODE_PRIVATE)

    fun getReminderIntervalMinutes(): Int = prefs.getInt(KEY_INTERVAL, 60)

    fun setReminderIntervalMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_INTERVAL, minutes).apply()
    }

    fun getDailyGoal(): Int = prefs.getInt(KEY_DAILY_GOAL, 8)

    fun setDailyGoal(cups: Int) {
        prefs.edit().putInt(KEY_DAILY_GOAL, cups).apply()
    }

    fun isFloatingEnabled(): Boolean = prefs.getBoolean(KEY_FLOATING_ENABLED, false)

    fun setFloatingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_ENABLED, enabled).apply()
    }

    fun getTodayDrinkCount(): Int {
        val today = todayKey()
        if (today != prefs.getString(KEY_LAST_DATE, "")) {
            prefs.edit().putInt(KEY_DRINK_COUNT, 0).putString(KEY_LAST_DATE, today).apply()
            return 0
        }
        return prefs.getInt(KEY_DRINK_COUNT, 0)
    }

    fun recordDrink() {
        prefs.edit()
            .putInt(KEY_DRINK_COUNT, getTodayDrinkCount() + 1)
            .putString(KEY_LAST_DATE, todayKey())
            .apply()
    }

    fun getQuietStart(): String = prefs.getString(KEY_QUIET_START, "22:00") ?: "22:00"
    fun setQuietStart(time: String) { prefs.edit().putString(KEY_QUIET_START, time).apply() }

    fun getQuietEnd(): String = prefs.getString(KEY_QUIET_END, "08:00") ?: "08:00"
    fun setQuietEnd(time: String) { prefs.edit().putString(KEY_QUIET_END, time).apply() }

    fun isVibrateEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATE, true)
    fun setVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()
    }

    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        private const val KEY_INTERVAL = "reminder_interval"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_FLOATING_ENABLED = "floating_enabled"
        private const val KEY_DRINK_COUNT = "drink_count"
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_QUIET_START = "quiet_start"
        private const val KEY_QUIET_END = "quiet_end"
        private const val KEY_VIBRATE = "vibrate"
    }
}
