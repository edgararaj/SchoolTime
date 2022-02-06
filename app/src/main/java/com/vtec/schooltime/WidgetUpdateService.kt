package com.vtec.schooltime

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class WidgetUpdateService: Service() {
    private val tag = javaClass.simpleName
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent != null)
            {
                Log.d(tag, "Received " + intent.action)
                updateWidget(context)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notification = NotificationCompat.Builder(this, App.notificationChannelId).build()
        startForeground(1, notification)

        applicationContext.registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_SCREEN_ON)
        })
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        applicationContext.unregisterReceiver(broadcastReceiver)
        return super.onDestroy()
    }
}