package com.vtec.schooltime

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URL
import java.util.*

fun getBeforeSubjectText(context: Context, startDeltaTime: Time): String
{
    val time = if (startDeltaTime.hour == 0) startDeltaTime.minute else startDeltaTime.averageHour
    val timeNameResId = if (startDeltaTime.hour == 0) if (startDeltaTime.minute == 1) R.string.minute else R.string.minutes else if (startDeltaTime.averageHour == 1) R.string.hour else R.string.hours
    val timeName = context.getString(timeNameResId)
    val widgetSubjectStartingString = context.getString(R.string.widget_subject_starting)

    return widgetSubjectStartingString.format(time, timeName)
}

fun getDuringSubjectTextAndColor(context: Context, schoolSubject: SchoolSubject, endDeltaTime: Time): Pair<String, Int>
{
    val timeNameResId = if (endDeltaTime.minute == 1) R.string.minute else R.string.minutes
    val timeName = context.getString(timeNameResId)

    val text =
        if (endDeltaTime.hour == 0)
            context.getString(R.string.widget_subject_ending_minutes).format(endDeltaTime.minute, timeName)
        else
            context.getString(R.string.widget_subject_ending_hours).format(endDeltaTime.hour, endDeltaTime.minute)

    return Pair(text, schoolSubject.color)
}

class ScheduleBlockSearch(val type: Int, val schoolSubject: SchoolSubject?, val deltaTime: Time?, val scheduleBlockIndex: Int?)

fun getCurrentScheduleBlock(schedule: SchoolSchedule, schoolSubjects: SchoolSubjects): ScheduleBlockSearch {
    val calendar = Calendar.getInstance()
    /*
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 15)
    calendar.set(Calendar.MINUTE, 50)
     */
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

    var type = R.string.free_day
    var schoolSubject: SchoolSubject? = null
    var deltaTime: Time? = null
    var scheduleBlockIndex: Int? = null

    val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
    for (scheduleBlock in scheduleBlocks) {
        schoolSubject = schoolSubjects[scheduleBlock.id] ?: continue
        val startDeltaTime = scheduleBlock.startTime - currentTime
        if (startDeltaTime > 0) {
            type = R.string.before_subject
            deltaTime = startDeltaTime
            break
        } else {
            val endDeltaTime = scheduleBlock.endTime - currentTime
            if (endDeltaTime > 0) {
                type = R.string.during_subject
                deltaTime = endDeltaTime
                scheduleBlockIndex = scheduleBlocks.indexOf(scheduleBlock)
                break
            } else if (scheduleBlock == schedule[currentDayOfWeek]?.last()) {
                val deltaTime = currentTime - scheduleBlock.endTime
                if (deltaTime <= Time(0, 5)) {
                    type = R.string.end_of_school
                }
            }
        }
    }

    return ScheduleBlockSearch(type, schoolSubject, deltaTime, scheduleBlockIndex)
}

fun getWidgetSchoolState(context: Context, scheduleBlockSearch: ScheduleBlockSearch): WidgetSchoolState
{
    var text = Widget.customization[R.string.free_day]?.customMsg ?: ""
    var bgColor = Widget.customization[R.string.free_day]?.bgColor ?: context.getColor(R.color.app_bg)
    var fgColor = Widget.customization[R.string.free_day]?.fgColor ?: context.getColor(R.color.app_fg)
    var alpha = Widget.customization[R.string.free_day]?.alpha ?: 1f
    var iconType = Widget.customization[R.string.free_day]?.iconType ?: R.string.widget_edit_icon

    when (scheduleBlockSearch.type)
    {
        R.string.before_subject -> {
            text = getBeforeSubjectText(context, scheduleBlockSearch.deltaTime!!)
            Widget.customization[R.string.before_subject]?.let {
                it.bgColor?.let { bgColor = it }
                it.fgColor?.let { fgColor = it }
                alpha = it.alpha
                iconType = it.iconType
            }
        }
        R.string.during_subject -> {
            val result = getDuringSubjectTextAndColor(context, scheduleBlockSearch.schoolSubject!!, scheduleBlockSearch.deltaTime!!)
            text = result.first
            bgColor = result.second
            fgColor = getContrastingColor(bgColor)
            Widget.customization[R.string.during_subject]?.let {
                alpha = it.alpha
                iconType = it.iconType
            }
        }
        R.string.end_of_school -> {
            text = context.getString(R.string.fallback_end_of_school_msg)
            Widget.customization[R.string.end_of_school]?.let {
                it.bgColor?.let { bgColor = it }
                it.fgColor?.let { fgColor = it }
                alpha = it.alpha
                iconType = it.iconType
            }
        }
    }

    return WidgetSchoolState(text, bgColor, fgColor, alpha, iconType)
}

class WidgetSchoolState(val text: String, val bgColor: Int, val fgColor: Int, val alpha: Float, val iconType: Int)

fun updateSchoolWidget(context: Context, views: RemoteViews)
{
    val scheduleBlockSearch = getCurrentScheduleBlock(Widget.schedule, Widget.subjects)
    val widgetSchoolState = getWidgetSchoolState(context, scheduleBlockSearch)

    views.setTextViewText(R.id.text, Html.fromHtml(widgetSchoolState.text, Html.FROM_HTML_MODE_COMPACT))
    views.setInt(R.id.bg, "setColorFilter", widgetSchoolState.bgColor)
    views.setInt(R.id.bg, "setAlpha", (widgetSchoolState.alpha * 255).toInt())
    views.setTextColor(R.id.text, widgetSchoolState.fgColor)

    when (scheduleBlockSearch.type)
    {
        R.string.before_subject -> {
            views.setTextViewText(R.id.long_name, scheduleBlockSearch.schoolSubject!!.longName)
            views.setTextViewText(R.id.short_name, scheduleBlockSearch.schoolSubject.shortName)
            views.setTextColor(R.id.long_name, widgetSchoolState.fgColor)
            views.setTextColor(R.id.short_name, widgetSchoolState.fgColor)

            views.setViewVisibility(R.id.subject, View.VISIBLE)

            views.setViewVisibility(R.id.time_line, View.GONE)
        }
        R.string.during_subject -> {
            views.setTextViewText(R.id.long_name, scheduleBlockSearch.schoolSubject!!.longName)
            views.setTextViewText(R.id.short_name, scheduleBlockSearch.schoolSubject.shortName)
            views.setTextColor(R.id.long_name, widgetSchoolState.fgColor)
            views.setTextColor(R.id.short_name, widgetSchoolState.fgColor)

            views.setViewVisibility(R.id.subject, View.VISIBLE)

            views.setTextColor(R.id.startTime, widgetSchoolState.fgColor)
            views.setTextColor(R.id.endTime, widgetSchoolState.fgColor)

            views.setViewVisibility(R.id.time_line, View.VISIBLE)
        }
        R.string.end_of_school -> {
            views.setViewVisibility(R.id.subject, View.GONE)
            views.setViewVisibility(R.id.time_line, View.GONE)
        }
        R.string.free_day -> {
            views.setViewVisibility(R.id.subject, View.GONE)
            views.setViewVisibility(R.id.time_line, View.GONE)
        }
    }

    Widget.iconType = widgetSchoolState.iconType
    if (widgetSchoolState.iconType != R.string.widget_no_icon)
    {
        views.setInt(R.id.activity_button, "setColorFilter", widgetSchoolState.fgColor)
        when (widgetSchoolState.iconType)
        {
            R.string.widget_weather_icon -> {
                updateWidgetWeatherIcon(context, views)
            }
            else -> {
                views.setImageViewResource(R.id.activity_button, R.drawable.pen_icon)
                views.setViewVisibility(R.id.activity_button, View.VISIBLE)
            }
        }
    }
    else
    {
        views.setViewVisibility(R.id.activity_button, View.GONE)
    }

    run {
        val intent = Intent(context, Widget::class.java).apply { action = tapAction }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.bg, pendingIntent)
    }

    run {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.activity_button, pendingIntent)
    }
}

fun updateWidgetWeatherIcon(context: Context, views: RemoteViews)
{
    GlobalScope.launch(Dispatchers.IO) {
        val resId = getWidgetActivityButtonRes()
        views.setImageViewResource(R.id.activity_button, resId)
        views.setViewVisibility(R.id.activity_button, View.VISIBLE)
        val widget = ComponentName(context, Widget::class.java)
        AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
    }
}

fun updateWidget(context: Context)
{
    try {
        val data = File(context.getExternalFilesDir(null), "data.hdvt").readText().split("|")

        Widget.schedule.clear()
        run {
            val init = Json.decodeFromString<SchoolSchedule>(data[0])
            init.forEach { (t, u) ->
                if (Widget.schedule[t] == null) Widget.schedule[t] = mutableListOf()
                u.forEach { Widget.schedule[t]?.add(it) }
            }
        }

        Widget.subjects.clear()
        run {
            val init = Json.decodeFromString<SchoolSubjects>(data[1])
            init.forEach { (t, u) -> Widget.subjects[t] = u }
        }

        run {
            val file = File(context.getExternalFilesDir(null), "widget_customization.json")
            Widget.customization = Json.decodeFromStream(FileInputStream(file))
        }

    } catch (ex: Exception) {
        Log.d("FileIO", "App data not found or incorrect!")
    }

    Widget.schedule.forEach { entry ->
        entry.value.sortBy { it.startTime }
    }

    val views = RemoteViews(context.packageName, R.layout.widget)
    updateSchoolWidget(context, views)

    val widget = ComponentName(context, Widget::class.java)
    AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
}

/*
val iconBitmapCache = mutableMapOf<String, Bitmap>()

fun getWeatherForecastIconBitmap(context: Context): Bitmap?
{
    val iconCode = getWeatherForecastIconCode(context) ?: return null
    iconBitmapCache[iconCode]?.let { return it }

    var bitmap: Bitmap? = null

    try {
        val imageStream = URL("https://openweathermap.org/img/wn/$iconCode@2x.png").openConnection().getInputStream()
        bitmap = BitmapFactory.decodeStream(imageStream)
        if (bitmap != null) iconBitmapCache[iconCode] = bitmap
    } catch (ex: FileNotFoundException) {
        Log.d("HTTPS", "Failed to fetch weather forecast icon!")
    }

    return bitmap
}
 */


fun getWidgetActivityButtonRes(): Int
{
    return when (getWeatherForecastIconCode())
    {
        "01d" -> R.drawable.clear_sky_day_icon
        "01n" -> R.drawable.clear_sky_night_icon
        "02d" -> R.drawable.few_clouds_day_icon
        "02n" -> R.drawable.few_clouds_night_icon
        "03d", "03n" -> R.drawable.scattered_clouds_icon
        "04d", "04n" -> R.drawable.broken_clouds_icon
        "09d", "09n" -> R.drawable.shower_rain_icon
        "10d", "10n" -> R.drawable.rain_icon
        "11d", "11n" -> R.drawable.thunderstorm_icon
        "13d", "13n" -> R.drawable.snow_icon
        "50d", "50n" -> R.drawable.mist_icon
        else -> R.drawable.pen_icon
    }
    /*
    return when (getWeatherForecastIconCode(context))
    {
        "01d" -> R.drawable.a_3d_clear_sky_daymdpi
        "01n" -> R.drawable.a_3d_clear_sky_nightmdpi
        "02d" -> R.drawable.a_3d_few_clouds_daymdpi
        "02n" -> R.drawable.a_3d_few_clouds_nightmdpi
        "03d" -> R.drawable.a_3d_scattered_cloudsmdpi
        "03n" -> R.drawable.a_3d_scattered_clouds_nightmdpi
        "04d" -> R.drawable.a_3d_broken_cloudsmdpi
        "04n" -> R.drawable.a_3d_broken_clouds_nightmdpi
        "09d" -> R.drawable.a_3d_shower_rainmdpi
        "09n" -> R.drawable.a_3d_shower_rain_nightmdpi
        "10d" -> R.drawable.a_3d_rainmdpi
        "10n" -> R.drawable.a_3d_rain_nightmdpi
        "11d" -> R.drawable.a_3d_thunderstormmdpi
        "11n" -> R.drawable.a_3d_thunderstorm_nightmdpi
        "13d" -> R.drawable.a_3d_snow_iconmdpi
        "13n" -> R.drawable.a_3d_snow_icon_nightmdpi
        "50d" -> R.drawable.a_3d_mistmdpi
        "50n" -> R.drawable.a_3d_mist_nightmdpi
        else -> R.drawable.widget_edit_icon
    }
     */
}

fun getWeatherForecastIconCode(): String?
{
    val lat = MainActivity.weatherLocation.value?.lat
    val lon = MainActivity.weatherLocation.value?.lon
    val apikey = "cc752389952464c89015c0c7aa74fab1"
    var iconCode: String? = null

    try {
        val forecast = URL("https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&cnt=1&units=metric&appid=$apikey").readText()
        iconCode = JSONObject(forecast).getJSONArray("list").getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("icon")
    } catch (ex: FileNotFoundException) {
        Log.d("HTTPS", "Failed to fetch weather forecast!")
    }

    return iconCode
}