package com.vtec.schooltime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.VibrationEffect
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import kotlinx.serialization.Serializable
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

@Serializable
class Time(val hour: Int, val minute: Int) : Comparable<Time>
{
    operator fun minus(other: Time): Time {
        val timeInMinutes = hour * 60 + minute
        val otherTimeInMinutes = other.hour * 60 + other.minute
        val deltaMinutes = timeInMinutes - otherTimeInMinutes
        return Time(deltaMinutes / 60, deltaMinutes % 60)
    }

    operator fun plus(other: Time): Time {
        val timeInMinutes = hour * 60 + minute
        val otherTimeInMinutes = other.hour * 60 + other.minute
        val deltaMinutes = timeInMinutes + otherTimeInMinutes
        return Time(deltaMinutes / 60, deltaMinutes % 60)
    }

    val averageHour = hour + if (minute >= 30) 1 else 0

    operator fun compareTo(other: Int) = compareTo(Time(other, other))

    override operator fun compareTo(other: Time): Int
    {
        val result = hour.compareTo(other.hour)
        if (result == 0)
            return minute.compareTo(other.minute)
        return result
    }

    override fun toString() = "%02d:%02d".format(hour, minute)
}

@Serializable
class SchoolSubject(var shortName: String, var longName: String, var color: Int)

@Serializable
class ScheduleBlock(val id: String, var startTime: Time, var duration: Time)
{
    val endTime get() = startTime + duration
}

typealias DayOfWeekSchedule = MutableList<ScheduleBlock>
typealias SchoolSchedule = MutableMap<Int, DayOfWeekSchedule>
typealias SchoolSubjects = MutableMap<String, SchoolSubject>

fun getContrastingColor(color: Int): Int
{
    return Color.HSVToColor(FloatArray(3).apply {
        this[2] = if (Color.luminance(color) > 0.5) 0F else 1F
    })
}

fun getTransitionState(value1: Int, value2: Int, fraction: Float) = (value1 + (value2 - value1) * fraction).toInt()

fun getColorTransitionState(value1: Int, value2: Int, fraction: Float): Int
{
    return Color.rgb(
        getTransitionState(Color.red(value1), Color.red(value2), fraction),
        getTransitionState(Color.green(value1), Color.green(value2), fraction),
        getTransitionState(Color.blue(value1), Color.blue(value2), fraction)
    )
}

fun readTextFromUri(uri: Uri, contentResolver: ContentResolver): String {
    val stringBuilder = StringBuilder()
    contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
        }
    }
    return stringBuilder.toString()
}

fun getColorsTransitionState(values: IntArray, fraction: Float): Int
{
    val valuesIndex = ((values.size - 1) * fraction).toInt()
    if (valuesIndex > 0)
    {
        return getColorTransitionState(values[valuesIndex - 1], values[valuesIndex], fraction)
    }
    else if (values.size > 1)
        return getColorTransitionState(values[valuesIndex], values[valuesIndex + 1], fraction)

    return 0
}

fun getDarkerColor(color: Int): Int {
    return Color.HSVToColor(FloatArray(3).apply {
        Color.colorToHSV(color, this)
        this[2] *= 0.95f
    })
}

fun logCallback(tag: String, msg: String) {
    Log.d(tag, "------- [$msg] -------")
}


fun <T> MutableLiveData<T>.mutation(actions: (MutableLiveData<T>) -> Unit) {
    actions(this)
    this.value = this.value
}

fun <T> MutableLiveData<T>.notify() {
    this.value = this.value
}

val Context.isDarkMode get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val notificationChannelId = "SchoolTime"
        val littleVibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        updateTheme(preferences.getString("theme", "") ?: "")
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun updateTheme(themeKey: String)
    {
        when (themeKey) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun createNotificationChannel(context: Context)
    {
        val channel = NotificationChannel(notificationChannelId, notificationChannelId, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "theme") updateTheme(sharedPreferences?.getString(key, "") ?: "")
    }
}