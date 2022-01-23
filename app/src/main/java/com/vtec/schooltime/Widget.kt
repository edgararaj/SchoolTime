package com.vtec.schooltime

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.Observer

const val tapAction = "com.vtec.schooltime.TAP"

class Widget : AppWidgetProvider() {
    private val tag = javaClass.simpleName
    private val doubleClickWindow = 400

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        context?.applicationContext?.let {
            it.startForegroundService(Intent(it, WidgetUpdateService::class.java))

            val observer = Observer<Any> {
                val views = RemoteViews(context.packageName, R.layout.widget)
                updateWidget(context, views)
                val widget = ComponentName(context, Widget::class.java)
                AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
            }

            MainActivity.didLessonsUpdate.observeForever(observer)
            MainActivity.didSchedulesUpdate.observeForever(observer)
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        context?.applicationContext?.let {
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
                updateWidget(context, views)
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
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://aecarlosamarante.pt/Horarios_ESCA/Tur1A_12K.pdf")).let {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                    }
                    return
                }
                sp.edit().putLong("firstClickTime", System.currentTimeMillis()).apply()

                val views = RemoteViews(context.packageName, R.layout.widget)
                updateWidget(context, views)
                val widget = ComponentName(context, Widget::class.java)
                AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
            }
        }
    }
}
