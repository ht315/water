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
    fun setReminderIntervalMinutes(minutes: Int) = prefs.edit().putInt(KEY_INTERVAL, minutes).apply()

    fun getDailyGoal(): Int = prefs.getInt(KEY_DAILY_GOAL, 8)
    fun setDailyGoal(cups: Int) = prefs.edit().putInt(KEY_DAILY_GOAL, cups).apply()

    fun isFloatingEnabled(): Boolean = prefs.getBoolean(KEY_FLOATING_ENABLED, false)
    fun setFloatingEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FLOATING_ENABLED, enabled).apply()

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
    fun setQuietStart(time: String) = prefs.edit().putString(KEY_QUIET_START, time).apply()

    fun getQuietEnd(): String = prefs.getString(KEY_QUIET_END, "08:00") ?: "08:00"
    fun setQuietEnd(time: String) = prefs.edit().putString(KEY_QUIET_END, time).apply()

    fun isVibrateEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATE, true)
    fun setVibrateEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()

    // --- Band vibration ---
    fun isBandVibrateEnabled(): Boolean = prefs.getBoolean(KEY_BAND_VIBRATE, false)
    fun setBandVibrateEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BAND_VIBRATE, enabled).apply()

    // --- Module switches ---
    fun isAttendanceModuleEnabled(): Boolean = prefs.getBoolean(KEY_MODULE_ATTENDANCE, false)
    fun setAttendanceModuleEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MODULE_ATTENDANCE, enabled).apply()

    fun isSedentaryModuleEnabled(): Boolean = prefs.getBoolean(KEY_MODULE_SEDENTARY, false)
    fun setSedentaryModuleEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MODULE_SEDENTARY, enabled).apply()

    fun isBedtimeModuleEnabled(): Boolean = prefs.getBoolean(KEY_MODULE_BEDTIME, false)
    fun setBedtimeModuleEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MODULE_BEDTIME, enabled).apply()

    // --- Attendance ---
    fun getTodayShift(): String = prefs.getString(KEY_TODAY_SHIFT, "") ?: ""
    fun setTodayShift(shift: String) = prefs.edit().putString(KEY_TODAY_SHIFT, shift).apply()

    fun getTodayShiftDate(): String = prefs.getString(KEY_TODAY_SHIFT_DATE, "") ?: ""
    fun setTodayShiftDate(date: String) = prefs.edit().putString(KEY_TODAY_SHIFT_DATE, date).apply()

    fun isTodayShiftSelected(): Boolean {
        val savedDate = getTodayShiftDate()
        if (savedDate != todayKey()) {
            setTodayShift("")
            setTodayShiftDate("")
            return false
        }
        return getTodayShift().isNotEmpty()
    }

    fun getAttendanceMorningTime(): String = prefs.getString(KEY_ATTENDANCE_MORNING_TIME, "06:00") ?: "06:00"
    fun setAttendanceMorningTime(time: String) = prefs.edit().putString(KEY_ATTENDANCE_MORNING_TIME, time).apply()

    fun getAttendanceMorningEndTime(): String = prefs.getString(KEY_ATTENDANCE_MORNING_END_TIME, "14:00") ?: "14:00"
    fun setAttendanceMorningEndTime(time: String) = prefs.edit().putString(KEY_ATTENDANCE_MORNING_END_TIME, time).apply()

    fun getAttendanceNightTime(): String = prefs.getString(KEY_ATTENDANCE_NIGHT_TIME, "14:00") ?: "14:00"
    fun setAttendanceNightTime(time: String) = prefs.edit().putString(KEY_ATTENDANCE_NIGHT_TIME, time).apply()

    fun getAttendanceNightEndTime(): String = prefs.getString(KEY_ATTENDANCE_NIGHT_END_TIME, "22:00") ?: "22:00"
    fun setAttendanceNightEndTime(time: String) = prefs.edit().putString(KEY_ATTENDANCE_NIGHT_END_TIME, time).apply()

    fun getAttendanceFullTime(): String = prefs.getString(KEY_ATTENDANCE_FULL_TIME, "08:00") ?: "08:00"
    fun setAttendanceFullTime(time: String) = prefs.edit().putString(KEY_ATTENDANCE_FULL_TIME, time).apply()

    fun getAttendanceFullEndTime(): String = prefs.getString(KEY_ATTENDANCE_FULL_END_TIME, "18:00") ?: "18:00"
    fun setAttendanceFullEndTime(time: String) = prefs.edit().putString(KEY_ATTENDANCE_FULL_END_TIME, time).apply()

    fun isAttendanceVibrateEnabled(): Boolean = prefs.getBoolean(KEY_ATTENDANCE_VIBRATE, true)
    fun setAttendanceVibrateEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_ATTENDANCE_VIBRATE, enabled).apply()

    fun getAttendanceCity(): String = prefs.getString(KEY_ATTENDANCE_CITY, "北京") ?: "北京"
    fun setAttendanceCity(city: String) = prefs.edit().putString(KEY_ATTENDANCE_CITY, city).apply()

    // --- Sedentary ---
    fun getSedentaryInterval(): Int = prefs.getInt(KEY_SEDENTARY_INTERVAL, 45)
    fun setSedentaryInterval(minutes: Int) = prefs.edit().putInt(KEY_SEDENTARY_INTERVAL, minutes).apply()

    fun getSedentaryStart(): String = prefs.getString(KEY_SEDENTARY_START, "09:00") ?: "09:00"
    fun setSedentaryStart(time: String) = prefs.edit().putString(KEY_SEDENTARY_START, time).apply()

    fun getSedentaryEnd(): String = prefs.getString(KEY_SEDENTARY_END, "18:00") ?: "18:00"
    fun setSedentaryEnd(time: String) = prefs.edit().putString(KEY_SEDENTARY_END, time).apply()

    fun isSedentaryVibrateEnabled(): Boolean = prefs.getBoolean(KEY_SEDENTARY_VIBRATE, true)
    fun setSedentaryVibrateEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SEDENTARY_VIBRATE, enabled).apply()

    // --- Bedtime ---
    fun getBedtimeTime(): String = prefs.getString(KEY_BEDTIME_TIME, "23:00") ?: "23:00"
    fun setBedtimeTime(time: String) = prefs.edit().putString(KEY_BEDTIME_TIME, time).apply()

    fun getBedtimeWakeUpTime(): String = prefs.getString(KEY_BEDTIME_WAKEUP, "07:00") ?: "07:00"
    fun setBedtimeWakeUpTime(time: String) = prefs.edit().putString(KEY_BEDTIME_WAKEUP, time).apply()

    fun getBedtimeAdvance(): Int = prefs.getInt(KEY_BEDTIME_ADVANCE, 0)
    fun setBedtimeAdvance(minutes: Int) = prefs.edit().putInt(KEY_BEDTIME_ADVANCE, minutes).apply()

    fun isBedtimeLockEnabled(): Boolean = prefs.getBoolean(KEY_BEDTIME_LOCK, false)
    fun setBedtimeLockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BEDTIME_LOCK, enabled).apply()

    fun isBedtimeVibrateEnabled(): Boolean = prefs.getBoolean(KEY_BEDTIME_VIBRATE, true)
    fun setBedtimeVibrateEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BEDTIME_VIBRATE, enabled).apply()

    // --- Custom reminders ---
    fun getCustomReminderCount(): Int = prefs.getInt(KEY_CUSTOM_COUNT, 0)
    fun setCustomReminderCount(count: Int) = prefs.edit().putInt(KEY_CUSTOM_COUNT, count).apply()

    fun getCustomReminderLabel(id: Int): String = prefs.getString("custom_${id}_label", "") ?: ""
    fun setCustomReminderLabel(id: Int, label: String) = prefs.edit().putString("custom_${id}_label", label).apply()

    fun getCustomReminderTime(id: Int): String = prefs.getString("custom_${id}_time", "08:00") ?: "08:00"
    fun setCustomReminderTime(id: Int, time: String) = prefs.edit().putString("custom_${id}_time", time).apply()

    fun isCustomReminderEnabled(id: Int): Boolean = prefs.getBoolean("custom_${id}_enabled", true)
    fun setCustomReminderEnabled(id: Int, enabled: Boolean) = prefs.edit().putBoolean("custom_${id}_enabled", enabled).apply()

    fun getCustomReminderRepeat(id: Int): String = prefs.getString("custom_${id}_repeat", "daily") ?: "daily"
    fun setCustomReminderRepeat(id: Int, repeat: String) = prefs.edit().putString("custom_${id}_repeat", repeat).apply()

    fun getCustomReminderWeekdays(id: Int): String = prefs.getString("custom_${id}_weekdays", "1,2,3,4,5") ?: "1,2,3,4,5"
    fun setCustomReminderWeekdays(id: Int, weekdays: String) = prefs.edit().putString("custom_${id}_weekdays", weekdays).apply()

    fun isCustomReminderVibrateEnabled(id: Int): Boolean = prefs.getBoolean("custom_${id}_vibrate", true)
    fun setCustomReminderVibrateEnabled(id: Int, enabled: Boolean) = prefs.edit().putBoolean("custom_${id}_vibrate", enabled).apply()

    fun deleteCustomReminder(id: Int) {
        val editor = prefs.edit()
        // Remove the item
        editor.remove("custom_${id}_label")
        editor.remove("custom_${id}_time")
        editor.remove("custom_${id}_enabled")
        editor.remove("custom_${id}_repeat")
        editor.remove("custom_${id}_weekdays")
        editor.remove("custom_${id}_vibrate")

        val count = getCustomReminderCount()
        // Shift remaining items down
        for (i in id + 1 until count) {
            editor.putString("custom_${i - 1}_label", getCustomReminderLabel(i))
            editor.putString("custom_${i - 1}_time", getCustomReminderTime(i))
            editor.putBoolean("custom_${i - 1}_enabled", isCustomReminderEnabled(i))
            editor.putString("custom_${i - 1}_repeat", getCustomReminderRepeat(i))
            editor.putString("custom_${i - 1}_weekdays", getCustomReminderWeekdays(i))
            editor.putBoolean("custom_${i - 1}_vibrate", isCustomReminderVibrateEnabled(i))
            // Remove old positions
            editor.remove("custom_${i}_label")
            editor.remove("custom_${i}_time")
            editor.remove("custom_${i}_enabled")
            editor.remove("custom_${i}_repeat")
            editor.remove("custom_${i}_weekdays")
            editor.remove("custom_${i}_vibrate")
        }
        editor.putInt(KEY_CUSTOM_COUNT, count - 1)
        editor.apply()
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
        private const val KEY_BAND_VIBRATE = "band_vibrate"

        private const val KEY_MODULE_ATTENDANCE = "module_attendance_enabled"
        private const val KEY_MODULE_SEDENTARY = "module_sedentary_enabled"
        private const val KEY_MODULE_BEDTIME = "module_bedtime_enabled"

        private const val KEY_TODAY_SHIFT = "today_shift"
        private const val KEY_TODAY_SHIFT_DATE = "today_shift_date"
        private const val KEY_ATTENDANCE_MORNING_TIME = "attendance_morning_time"
        private const val KEY_ATTENDANCE_MORNING_END_TIME = "attendance_morning_end_time"
        private const val KEY_ATTENDANCE_NIGHT_TIME = "attendance_night_time"
        private const val KEY_ATTENDANCE_NIGHT_END_TIME = "attendance_night_end_time"
        private const val KEY_ATTENDANCE_FULL_TIME = "attendance_full_time"
        private const val KEY_ATTENDANCE_FULL_END_TIME = "attendance_full_end_time"
        private const val KEY_ATTENDANCE_VIBRATE = "attendance_vibrate"
        private const val KEY_ATTENDANCE_CITY = "attendance_city"

        private const val KEY_SEDENTARY_INTERVAL = "sedentary_interval"
        private const val KEY_SEDENTARY_START = "sedentary_start"
        private const val KEY_SEDENTARY_END = "sedentary_end"
        private const val KEY_SEDENTARY_VIBRATE = "sedentary_vibrate"

        private const val KEY_BEDTIME_TIME = "bedtime_time"
        private const val KEY_BEDTIME_WAKEUP = "bedtime_wakeup_time"
        private const val KEY_BEDTIME_ADVANCE = "bedtime_advance"
        private const val KEY_BEDTIME_LOCK = "bedtime_lock_enabled"
        private const val KEY_BEDTIME_VIBRATE = "bedtime_vibrate"

        private const val KEY_CUSTOM_COUNT = "custom_reminder_count"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingDone() = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
}
