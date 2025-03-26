package net.muskit.questcamstreamer

import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.muskit.questcamstreamer.global.Settings
import net.muskit.questcamstreamer.global.State
import net.muskit.questcamstreamer.video.Camera
import net.muskit.questcamstreamer.video.ImageStreamer
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.net.Socket
import java.net.URL

class StreamService: LifecycleService() {
    private val TAG = "StreamService"

    private lateinit var connectingCoroutine: CoroutineScope

    private lateinit var useCase: ImageAnalysis
    private lateinit var notifBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.END.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.d(TAG, "start")
        State.streamService = this
        notifBuilder = NotificationCompat.Builder(this, "status_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("You are sharing your camera and microphone.")
            .setOngoing(true)

        setStatus(Status.CONNECTING, false)
        startForeground(1, notifBuilder.build())

        connectingCoroutine = CoroutineScope(Dispatchers.IO)
        connectingCoroutine.launch {
            val connSuccess = tryConnect()
            CoroutineScope(Dispatchers.Main).launch {
                if (connSuccess) {
                    setStatus(Status.CONNECTED)
                    setCam(Settings.streamCam)
                } else {
                    setStatus(Status.DISCONNECTED)
                    stop()
                }
            }
        }
    }

    private fun setStatus(status: Status, updateExistingNotif: Boolean = false) {
        sendBroadcast(Intent(CONNECTION_STATUS_CHANGED).putExtra("status", status))
        val txt = status.toString().lowercase()
//        txt.replaceFirstChar { it.uppercaseChar() }
        notifBuilder.setContentTitle("Quest Cam Streamer is $txt")

        if (updateExistingNotif) {
            val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notifMan.notify(1, notifBuilder.build())
        }
    }

    private fun tryConnect(): Boolean {
        val url = URL("http://${Settings.connectionString}")
        val host = url.host
        val port = url.port

        // establish connection
        Log.d(TAG, "tryConnect: connecting to $host:$port")
        val socket: Socket
        try {
            Log.d(TAG, "tryConnect: socket creation...")
            socket = Socket(host, port)
            Log.d(TAG, "tryConnect: socket connected!")
        } catch (e: Exception) {
            Log.e(TAG, "tryConnect: error $e")
            return false
        }

        // exchange RTC offer
        // send video specs (framerate, resolution)
        Log.d(TAG, "tryConnect: sending offer")
        val stream = socket.getOutputStream()
        stream.write("yummers".toByteArray())
        Log.d(TAG, "tryConnect: offser sent")

        // get answer
        val resp = byteArrayOf()
        val inStream = DataInputStream(BufferedInputStream(socket.getInputStream()))

//        do {
//            inStream.readFully(resp)
//        } while (resp.isNotEmpty())

        Log.d(TAG, "tryConnect: reading answer")
        inStream.readFully(resp)
        val str = resp.decodeToString()
        Log.d(TAG, "tryConnect: got answer: $str (${str.length} bytes)")

        socket.close()

        // establish RTC streaming connection

        Log.d(TAG, "tryConnect: done")
        return false
    }

    public fun setCam(on: Boolean) {
        if (on) {
            useCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            useCase.setAnalyzer(mainExecutor, ImageStreamer())
            Camera.bindUsecase(this, useCase, "stream")
        } else { // off
            Camera.unbindUsecase("stream")
        }
        Camera.refreshUsecasesLifecycle()
    }

    private fun stop() {
        Log.d(TAG, "stop")

        if (connectingCoroutine.isActive) {
            connectingCoroutine.cancel()
        }
        State.streamService = null
        setCam(false)
        setStatus(Status.DISCONNECTED)
        stopSelf()
    }

    enum class Status {
        DISCONNECTED, CONNECTING, CONNECTED
    }
    enum class Actions {
        START, END
    }
    companion object {
        val CONNECTION_STATUS_CHANGED = "net.muskit.QuestCamStreamer_CONNECTION_STATUS"
    }
}