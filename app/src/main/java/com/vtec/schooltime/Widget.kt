package com.vtec.schooltime

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager

const val tapAction = "com.vtec.schooltime.TAP"

class Widget : AppWidgetProvider() {
    private val tag = javaClass.simpleName
    private val doubleClickWindow = 400

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        if (context != null)
        {
            context.startService(Intent(context, WidgetUpdateService::class.java))

            MainActivity.didLessonsUpdate.observeForever {
                updateWidget(context)
            }
            MainActivity.didSchedulesUpdate.observeForever {
                updateWidget(context)
            }
            MainActivity.weatherLocation.observeForever {
                val views = RemoteViews(context.packageName, R.layout.widget)
                drawWidgetActivityButton(context, views)
            }
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
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
            }
        }
    }
}
