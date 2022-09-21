package com.vtec.schooltime

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.vtec.schooltime.activities.WidgetCustomization

const val tapAction = "com.vtec.schooltime.TAP"

class Widget : AppWidgetProvider() {
    private val tag = javaClass.simpleName
    private val doubleClickWindow = 400

    companion object {
        var schedule: SchoolSchedule = mutableMapOf()
        val subjects: SchoolSubjects = mutableMapOf()
        var customization: WidgetCustomization = mutableMapOf()
        var iconType: Int? = null
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        logCallback(tag, "onEnabled")
        if (context != null)
        {
            context.startForegroundService(Intent(context, WidgetUpdateService::class.java))

            MainActivity.didSubjectsUpdate.observeForever {
                updateWidget(context)
            }
            MainActivity.didSchedulesUpdate.observeForever {
                updateWidget(context)
            }
            MainActivity.weatherLocation.observeForever {
                val views = RemoteViews(context.packageName, R.layout.widget)
                if (iconType == R.string.widget_weather_icon)
                    updateWidgetWeather(context, views)
            }
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        logCallback(tag, "onDisabled")
        context?.let {
            it.stopService(Intent(it, WidgetUpdateService::class.java))
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        logCallback(tag, "onUpdate")
        if (context != null)
        {
            appWidgetIds?.forEach { appWidgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget)
                updateSchoolWidget(context, views)
                appWidgetManager?.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        logCallback(tag, "onReceive")
        if (context != null && intent != null)
        {
            Log.d(tag, "Received " + intent.action)
            if (intent.action == tapAction)
            {
                val sp = context.getSharedPreferences(tag, Context.MODE_PRIVATE)

                if (System.currentTimeMillis() - sp.getLong("firstClickTime", 0) <= doubleClickWindow)
                {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val link = preferences.getString("open_link", "") ?: ""
                    if (link.isNotEmpty())
                    {
                        Intent(Intent.ACTION_VIEW, Uri.parse(link)).let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    }
                    return
                }
                sp.edit().putLong("firstClickTime", System.currentTimeMillis()).apply()

                updateWidget(context)
                context.startForegroundService(Intent(context, WidgetUpdateService::class.java))
            }
        }
    }
}
