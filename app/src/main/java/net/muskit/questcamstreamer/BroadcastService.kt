package net.muskit.questcamstreamer

import android.app.Notification
import android.content.Intent
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import java.net.URL

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
            .setContentTitle("Broadcasting IRL Sensors")
            .setContentText("Quest Cam Streamer is broadcasting your camera and microphone.")
            .setOngoing(true)
            .build()
        startForeground(1, notification)

        if (tryConnect()){
            setCam(Settings.broadcastCam)
        }
    }

    private fun tryConnect(): Boolean {
        val url = URL("http://${Settings.connectionString}")
        val host = url.host
        val port = url.port

        // establish connection

        // send video specs (framerate, resolution)

        return true
    }

    public fun setCam(on: Boolean) {
        if (on) {
            useCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            useCase.setAnalyzer(mainExecutor, ImageBroadcaster())
            Camera.bindUsecase(this, useCase, "broadcast")
        } else { // off
            Camera.unbindUsecase("broadcast")
        }
        Camera.refreshUsecasesLifecycle()
    }

    private fun stop() {
        Log.d("BroadcastService", "stop")

        setCam(false)

        State.broadcastService = null
        stopSelf()
    }

    enum class Actions {
        START, END
    }
}