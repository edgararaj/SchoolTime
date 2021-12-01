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
    var bgColor = context.getColor(if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) R.color.none_dark else R.color.none)
    val schedule = currentSchedule?.value
    val schoolClasses = currentSchoolClasses?.value

    if (schedule != null && schoolClasses != null) {
        val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
        if (scheduleBlocks != null)
        {
            for (scheduleBlock in scheduleBlocks)
            {
                val startDeltaTime = scheduleBlock.startTime - currentTime
                if (startDeltaTime > 0)
                {
                    val schoolClass = schoolClasses[scheduleBlock.schoolClassId]
                    if (schoolClass != null)
                    {
                        val time = if (startDeltaTime.hour == 0) startDeltaTime.minute else startDeltaTime.averageHour
                        val timeName = if (startDeltaTime.hour == 0) if (startDeltaTime.minute == 1) "minuto" else "minutos" else if (startDeltaTime.averageHour == 1) "hora" else "horas"
                        text = "Aula de <b>%s</b> começa em <br><b>%d %s</b>".format(schoolClass.shortName, time, timeName)
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
                            val timeName = if (endDeltaTime.minute == 1) "minuto" else "minutos"
                            val faltar = if (endDeltaTime.hour == 0) if (endDeltaTime.minute == 1) "Falta" else "Faltam" else if (endDeltaTime.hour == 1) "Falta" else "Faltam"
                            text = if (endDeltaTime.hour == 0)
                                "%s <b>%d %s</b> para acabar <b>%s</b>".format(faltar, endDeltaTime.minute, timeName, schoolClass.shortName)
                            else
                                "%s <b>%02d:%02d</b> para acabar <b>%s</b>".format(faltar, endDeltaTime.hour, endDeltaTime.minute, schoolClass.shortName)
                            bgColor = schoolClass.color
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
