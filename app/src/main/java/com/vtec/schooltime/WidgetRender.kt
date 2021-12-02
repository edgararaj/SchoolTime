package com.vtec.schooltime

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.text.Html
import android.widget.RemoteViews
import java.util.*

fun updateWidget(context: Context, views: RemoteViews)
{
    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    var text = ""
    var bgColor = context.getColor(R.color.app_bg)
    val schedule = currentSchedule?.value
    val schoolClasses = currentSchoolClasses?.value

    if (schedule != null && schoolClasses != null) {
        val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
        if (scheduleBlocks != null)
        {
            for (scheduleBlock in scheduleBlocks)
            {
                val startDeltaTime = scheduleBlock.startTime - currentTime
                val language = Locale.getDefault().language
                if (startDeltaTime > 0)
                {
                    val schoolClass = schoolClasses[scheduleBlock.schoolClassId]
                    if (schoolClass != null)
                    {
                        val time = if (startDeltaTime.hour == 0) startDeltaTime.minute else startDeltaTime.averageHour
                        val timeNameResId = if (startDeltaTime.hour == 0) if (startDeltaTime.minute == 1) R.string.minute else R.string.minutes else if (startDeltaTime.averageHour == 1) R.string.hour else R.string.hours
                        val timeName = context.getString(timeNameResId)
                        val widgetClassStartingString = context.getString(R.string.widget_class_starting)

                        text = when (language) {
                            "pt" -> widgetClassStartingString.format(schoolClass.shortName, time, timeName)
                            else -> widgetClassStartingString.format(schoolClass.shortName, time, timeName)
                        }
                    }
                    break
                }
                else {
                    val endDeltaTime = scheduleBlock.endTime - currentTime
                    if (endDeltaTime > 0)
                    {
                        val schoolClass = schoolClasses[scheduleBlock.schoolClassId]
                        if (schoolClass != null)
                        {
                            bgColor = schoolClass.color
                            val timeNameResId = if (endDeltaTime.minute == 1) R.string.minute else R.string.minutes
                            val timeName = context.getString(timeNameResId)
                            val faltar = if (endDeltaTime.hour == 0) if (endDeltaTime.minute == 1) "Falta" else "Faltam" else if (endDeltaTime.hour == 1) "Falta" else "Faltam"

                            text = when (language) {
                                "pt" -> if (endDeltaTime.hour == 0)
                                    context.getString(R.string.widget_class_ending_minutes).format(faltar, endDeltaTime.minute, timeName, schoolClass.shortName)
                                else
                                    context.getString(R.string.widget_class_ending_hours).format(faltar, endDeltaTime.hour, endDeltaTime.minute, schoolClass.shortName)
                                else -> if (endDeltaTime.hour == 0)
                                    context.getString(R.string.widget_class_ending_minutes).format(endDeltaTime.minute, timeName, schoolClass.shortName)
                                else
                                    context.getString(R.string.widget_class_ending_hours).format(endDeltaTime.hour, endDeltaTime.minute, schoolClass.shortName)
                            }
                        }
                        break
                    }
                    else if (scheduleBlock == schedule[currentDayOfWeek]?.last())
                    {
                        val deltaTime = currentTime - scheduleBlock.endTime
                        if (deltaTime <= Time(0, 5))
                        {
                            text = "Acabaram-se as aulas por hoje!<br>Parabéns! 🎆"
                            bgColor = context.getColor(R.color.finished)
                        }
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
