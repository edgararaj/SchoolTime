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
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.util.*

fun getBeforeLessonText(context: Context, schoolLesson: SchoolLesson, startDeltaTime: Time): String
{
    val time = if (startDeltaTime.hour == 0) startDeltaTime.minute else startDeltaTime.averageHour
    val timeNameResId = if (startDeltaTime.hour == 0) if (startDeltaTime.minute == 1) R.string.minute else R.string.minutes else if (startDeltaTime.averageHour == 1) R.string.hour else R.string.hours
    val timeName = context.getString(timeNameResId)
    val widgetLessonStartingString =
        context.getString(R.string.widget_lesson_starting)

    return when (Locale.getDefault().language) {
        "pt" -> widgetLessonStartingString.format(
            schoolLesson.shortName,
            time,
            timeName
        )
        else -> widgetLessonStartingString.format(
            schoolLesson.shortName,
            time,
            timeName
        )
    }
}

fun getDuringLessonTextAndColor(context: Context, schoolLesson: SchoolLesson, endDeltaTime: Time): Pair<String, Int>
{
    val bgColor = schoolLesson.color
    val timeNameResId = if (endDeltaTime.minute == 1) R.string.minute else R.string.minutes
    val timeName = context.getString(timeNameResId)
    val faltar = if (endDeltaTime.hour == 0) if (endDeltaTime.minute == 1) "Falta" else "Faltam" else if (endDeltaTime.hour == 1) "Falta" else "Faltam"

    val text = when (Locale.getDefault().language) {
        "pt" -> if (endDeltaTime.hour == 0)
            context.getString(R.string.widget_lesson_ending_minutes).format(
                faltar,
                endDeltaTime.minute,
                timeName,
                schoolLesson.shortName
            )
        else
            context.getString(R.string.widget_lesson_ending_hours).format(
                faltar,
                endDeltaTime.hour,
                endDeltaTime.minute,
                schoolLesson.shortName
            )
        else -> if (endDeltaTime.hour == 0)
            context.getString(R.string.widget_lesson_ending_minutes)
                .format(endDeltaTime.minute, timeName, schoolLesson.shortName)
        else
            context.getString(R.string.widget_lesson_ending_hours).format(
                endDeltaTime.hour,
                endDeltaTime.minute,
                schoolLesson.shortName
            )
    }

    return Pair(text, bgColor)
}

fun getTextAndColor(context: Context): Pair<String, Int>
{
    var text = ""
    var bgColor = context.getColor(R.color.app_bg)

    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    val schedule = MainActivity.schedule
    val schoolLessons = MainActivity.lessons

    val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
    for (scheduleBlock in scheduleBlocks) {
        val schoolLesson = schoolLessons[scheduleBlock.schoolLessonId] ?: continue
        val startDeltaTime = scheduleBlock.startTime - currentTime
        if (startDeltaTime > 0) {
            text = getBeforeLessonText(context, schoolLesson, startDeltaTime)
            break
        } else {
            val endDeltaTime = scheduleBlock.endTime - currentTime
            if (endDeltaTime > 0) {
                val result = getDuringLessonTextAndColor(context, schoolLesson, startDeltaTime)
                text = result.first
                bgColor = result.second
                break
            } else if (scheduleBlock == schedule[currentDayOfWeek]?.last()) {
                val deltaTime = currentTime - scheduleBlock.endTime
                if (deltaTime <= Time(0, 5)) {
                    text = "Acabaram-se as aulas por hoje!<br>Parabéns! 🎆"
                    bgColor = context.getColor(R.color.finished)
                }
            }
        }
    }

    return Pair(text, bgColor)
}

fun updateSchoolWidget(context: Context, views: RemoteViews)
{
    val (text, bgColor) = getTextAndColor(context)
//    if (bgColor == context.getColor(R.color.app_bg))
//        views.setViewVisibility(R.id.bg, View.GONE)

    views.setTextViewText(R.id.text, Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT))
    views.setInt(R.id.bg, "setColorFilter", bgColor)
    val contrastyFgColor = getContrastingColor(bgColor)
    views.setTextColor(R.id.text, contrastyFgColor)
    views.setInt(R.id.activity_button, "setColorFilter", contrastyFgColor)

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

fun drawWidgetActivityButton(context: Context, views: RemoteViews)
{
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val widgetIcon = preferences.getString("widget_icon", "")
    if (widgetIcon == "weather")
    {
        GlobalScope.launch(Dispatchers.IO) {
            val resId = getWidgetActivityButtonRes(context)
            views.setViewVisibility(R.id.activity_button, View.VISIBLE)
            views.setImageViewResource(R.id.activity_button, resId)
            val widget = ComponentName(context, Widget::class.java)
            AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
        }
    }
    else
    {
        if (widgetIcon == "no_icon")
            views.setViewVisibility(R.id.activity_button, View.GONE)
        else
        {
            views.setViewVisibility(R.id.activity_button, View.VISIBLE)
            views.setImageViewResource(R.id.activity_button, R.drawable.widget_edit_icon)
        }
        val widget = ComponentName(context, Widget::class.java)
        AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
    }
}

fun updateWidget(context: Context)
{
    val views = RemoteViews(context.packageName, R.layout.widget)

    drawWidgetActivityButton(context, views)
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


fun getWidgetActivityButtonRes(context: Context): Int
{
    val iconCode = getWeatherForecastIconCode(context) ?: return R.drawable.widget_edit_icon

    return when (iconCode)
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
        else -> R.drawable.widget_edit_icon
    }
}

fun getWeatherForecastIconCode(context: Context): String?
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