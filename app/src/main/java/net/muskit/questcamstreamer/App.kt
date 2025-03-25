package net.muskit.questcamstreamer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "status_channel",
            "Broadcast Status",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifMan.createNotificationChannel(channel)
    }
}