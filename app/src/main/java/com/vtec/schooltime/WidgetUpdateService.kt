package com.vtec.schooltime

import android.app.Notification
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews

class WidgetUpdateService: Service() {
    private val tag = javaClass.simpleName
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent != null)
            {
                Log.d(tag, "Received " + intent.action)

                val views = RemoteViews(context.packageName, R.layout.widget)
                updateWidget(context, views)
                val widget = ComponentName(context, Widget::class.java)
                AppWidgetManager.getInstance(context).updateAppWidget(widget, views)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notification = Notification.Builder(this, App.notificationChannelId).build()

        startForeground(1, notification)
        applicationContext.registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
        })
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        applicationContext.unregisterReceiver(broadcastReceiver)
        return super.onDestroy()
    }
}