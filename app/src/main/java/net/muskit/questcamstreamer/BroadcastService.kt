package net.muskit.questcamstreamer

import android.app.Notification
import android.content.Intent
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService

class BroadcastService: LifecycleService() {
    private lateinit var useCase: ImageAnalysis
    public lateinit var notification: Notification

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.END.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.d("BroadcastService", "start")
        State.broadcastService = this
        notification = NotificationCompat.Builder(this, "status_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Broadcasting")
            .setContentText("camera, microphone")
            .setOngoing(true)
            .build()
        startForeground(1, notification)

        useCase = ImageAnalysis.Builder().build()
        useCase.setAnalyzer(mainExecutor, ImageBroadcaster())
        Camera.bindUsecase(this, useCase, "broadcast")
    }

    private fun stop() {
        Log.d("BroadcastService", "stop")
        State.broadcastService = null
        Camera.unbindUsecase("broadcast")
        Camera.refreshUsecasesLifecycle()
        stopSelf()
    }

    enum class Actions {
        START, END
    }
}