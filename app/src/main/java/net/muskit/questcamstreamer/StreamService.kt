package net.muskit.questcamstreamer

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.muskit.questcamstreamer.global.Settings
import net.muskit.questcamstreamer.global.State
import net.muskit.questcamstreamer.stream.RTCClient
import net.muskit.questcamstreamer.video.Camera
import net.muskit.questcamstreamer.video.ImageStreamer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.URL

class StreamService: LifecycleService() {
    private val TAG = "StreamService"
    private var stopped = false

    private lateinit var connectionCoroutine: CoroutineScope
    private lateinit var useCase: ImageAnalysis
    private lateinit var notifBuilder: NotificationCompat.Builder

    private var rtcClient: RTCClient? = null

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
        startForeground(
            1, notifBuilder.build(),
           FOREGROUND_SERVICE_TYPE_MANIFEST
        )

        connectionCoroutine = CoroutineScope(Dispatchers.IO)
        connectionCoroutine.launch { runConnection() }
    }

    private fun runConnection() {
        val url = URL("http://${Settings.connectionString}")
        val host = url.host
        val port = url.port

        // establish connection
        Log.d(TAG, "tryConnect: connecting to $host:$port")
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(host, port), 5000)
            Log.d(TAG, "tryConnect: connected!")
        } catch (e: Exception) {
            Log.e(TAG, "tryConnect: error $e")
            socket.close()
            setStatus(Status.DISCONNECTED)
            stop()
            return
        }

        // init WebRTC
        Log.d(TAG, "tryConnect: initializing webRTC")
        rtcClient = RTCClient(this, socket)
        rtcClient!!.createOffer()

        setStatus(Status.CONNECTED)
        setCam(Settings.streamCam)

        // listen for messages
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        while (true) {
            try {
                val message = reader.readLine() ?: break
                rtcClient!!.handleReceivedMessage(message)
            } catch (e: SocketException) {
                Log.d(TAG, "runConnection: message socket closed")
                break
            }
        }
        stop()
    }

    private fun setStatus(status: Status, updateExistingNotif: Boolean = true) {
        sendBroadcast(Intent(CONNECTION_STATUS_CHANGED).putExtra("status", status))
        val txt = status.toString().lowercase().replaceFirstChar { it.uppercaseChar() }
        notifBuilder.setContentTitle("Status: $txt")

        if (updateExistingNotif) {
            val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notifMan.notify(1, notifBuilder.build())
        }
    }

    public fun setCam(on: Boolean) {
        if (on && rtcClient != null) {
            useCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            useCase.setAnalyzer(mainExecutor, ImageStreamer(rtcClient!!))
            Camera.bindUsecase(this, useCase, "stream")
        } else { // off
            Camera.unbindUsecase("stream")
        }
         Camera.refreshUsecasesLifecycle()
    }

    private fun stop() {
        if (stopped) return
        stopped = true

        Log.d(TAG, "stop")

        if (connectionCoroutine.isActive) {
            connectionCoroutine.cancel()
        }
        State.streamService = null
        setCam(false)
        rtcClient?.close()
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