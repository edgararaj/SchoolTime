package com.vtec.schooltime

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.Html
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*

fun updateWidget(context: Context, views: RemoteViews)
{
    /*
    GlobalScope.launch(Dispatchers.IO) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val city = preferences.getString("weather_location", "")
        val apikey = "GnGNpZOydb8WwGfJc9q5tFh2xaABmxGc"
        val locationSearch =
            URL("https://dataservice.accuweather.com/locations/v1/cities/search?q=$city&apikey=$apikey").readText()
        val locationKey = JSONArray(locationSearch).getJSONObject(0).getInt("Key")
        val forecast =
            URL("https://dataservice.accuweather.com/forecasts/v1/daily/1day/$locationKey?apikey=$apikey").readText()
        val forecastIcon = JSONObject(forecast).getJSONArray("DailyForecasts").getJSONObject(0)
            .getJSONObject("Day").getInt("Icon")
        Log.d("------------", "$forecastIcon")
    }
     */

    var text = ""
    var bgColor = context.getColor(R.color.app_bg)

    run {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        val schedule = MainActivity.schedule
        val schoolLessons = MainActivity.lessons

        val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
        for (scheduleBlock in scheduleBlocks) {
            val startDeltaTime = scheduleBlock.startTime - currentTime
            val language = Locale.getDefault().language
            if (startDeltaTime > 0) {
                val schoolLesson = schoolLessons[scheduleBlock.schoolLessonId]
                if (schoolLesson != null) {
                    val time =
                        if (startDeltaTime.hour == 0) startDeltaTime.minute else startDeltaTime.averageHour
                    val timeNameResId =
                        if (startDeltaTime.hour == 0) if (startDeltaTime.minute == 1) R.string.minute else R.string.minutes else if (startDeltaTime.averageHour == 1) R.string.hour else R.string.hours
                    val timeName = context.getString(timeNameResId)
                    val widgetLessonStartingString =
                        context.getString(R.string.widget_lesson_starting)

                    text = when (language) {
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
                break
            } else {
                val endDeltaTime = scheduleBlock.endTime - currentTime
                if (endDeltaTime > 0) {
                    val schoolLesson = schoolLessons[scheduleBlock.schoolLessonId]
                    if (schoolLesson != null) {
                        bgColor = schoolLesson.color
                        val timeNameResId =
                            if (endDeltaTime.minute == 1) R.string.minute else R.string.minutes
                        val timeName = context.getString(timeNameResId)
                        val faltar =
                            if (endDeltaTime.hour == 0) if (endDeltaTime.minute == 1) "Falta" else "Faltam" else if (endDeltaTime.hour == 1) "Falta" else "Faltam"

                        text = when (language) {
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
                    }
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
    }

    views.setTextViewText(R.id.text, Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT))
    views.setInt(R.id.bg, "setColorFilter", bgColor)
    val contrastyFgColor = getContrastingColor(bgColor)
    views.setTextColor(R.id.text, contrastyFgColor)
    views.setInt(R.id.edit_button, "setColorFilter", contrastyFgColor)

    run {
        val intent = Intent(context, Widget::class.java).apply { action = tapAction }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.bg, pendingIntent)
    }

    run {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.edit_button, pendingIntent)
    }
}
