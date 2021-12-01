package com.vtec.schooltime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class Time(val hour: Int, val minute: Int)
{
    operator fun minus(other: Time): Time {
        val timeInMinutes = hour * 60 + minute
        val otherTimeInMinutes = other.hour * 60 + other.minute
        val deltaMinutes = timeInMinutes - otherTimeInMinutes
        return Time(deltaMinutes / 60, deltaMinutes % 60)
    }

    val averageHour = hour + if (minute >= 30) 1 else 0

    operator fun compareTo(other: Int) = compareTo(Time(other, other))

    operator fun compareTo(other: Time): Int
    {
        val result = hour.compareTo(other.hour)
        if (result == 0)
            return minute.compareTo(other.minute)
        return result
    }

    override fun toString() = "%02d:%02d".format(hour, minute)
}

@Serializable
class SchoolClass(var shortName: String, var longName: String, var color: Int)

@Serializable
class ScheduleBlock(val schoolClassId: String, var startTime: Time, var endTime: Time)

val fallbackSchoolClasses = mutableMapOf("MAT" to SchoolClass("MAT", "Matemática", Color.parseColor("#7f98e3")),
    "EF" to SchoolClass("EF", "Educação Física", Color.parseColor("#e3cc7f")),
    "FSC" to SchoolClass("FSC", "Física", Color.parseColor("#e3cc7f")),
    "PT" to SchoolClass("PT", "Português", Color.parseColor("#e37f7f")),
    "AINF" to SchoolClass("AINF", "Aplicações Informáticas", Color.parseColor("#7fe390"))
)

val fallbackSchedule = mutableMapOf(
    Calendar.MONDAY to mutableListOf(
    ScheduleBlock("MAT", Time(14, 0), Time(15, 30)),
    ScheduleBlock("EF", Time(15, 40), Time(17, 10)),
    ScheduleBlock("FSC", Time(17, 20), Time(18, 50)),
),
    Calendar.TUESDAY to mutableListOf(
        ScheduleBlock("PT", Time(13, 15), Time(13, 50)),
        ScheduleBlock("PT", Time(14, 0), Time(15, 30)),
        ScheduleBlock("FSC", Time(15, 40), Time(17, 10)),
        ScheduleBlock("MAT", Time(17, 20), Time(18, 50)),
    ),
    Calendar.WEDNESDAY to mutableListOf(),
    Calendar.THURSDAY to mutableListOf(
        ScheduleBlock("MAT", Time(14, 0), Time(15, 30)),
        ScheduleBlock("PT", Time(15, 40), Time(17, 10)),
        ScheduleBlock("AINF", Time(17, 20), Time(18, 50)),
    ),
    Calendar.FRIDAY to mutableListOf(
        ScheduleBlock("FSC", Time(14, 0), Time(15, 30)),
        ScheduleBlock("EF", Time(15, 40), Time(17, 10)),
        ScheduleBlock("AINF", Time(17, 20), Time(18, 50)),
    ))

typealias DayOfWeekSchedule = MutableList<ScheduleBlock>
typealias Schedule = MutableLiveData<MutableMap<Int, DayOfWeekSchedule>>
typealias SchoolClasses = MutableLiveData<MutableMap<String, SchoolClass>>

fun getContrastingColor(color: Int): Int
{
    return Color.HSVToColor(FloatArray(3).apply {
        this[2] = if (Color.luminance(color) > 0.5) 0F else 1F
    })
}

fun getDarkerColor(color: Int): Int {
    return Color.HSVToColor(FloatArray(3).apply {
        Color.colorToHSV(color, this)
        this[2] *= 0.9f
    })
}

fun <T> MutableLiveData<T>.mutation(actions: (MutableLiveData<T>) -> Unit) {
    actions(this)
    this.value = this.value
}

class App : Application() {
    companion object {
        const val notificationChannelId = "SchoolTime"
        @RequiresApi(Build.VERSION_CODES.Q)
        val littleVibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)
    }

    private fun createNotificationChannel(context: Context)
    {
        val channel = NotificationChannel(notificationChannelId, "Example", NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}