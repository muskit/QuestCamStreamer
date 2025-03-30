package net.muskit.questcamstreamer

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.muskit.questcamstreamer.global.Settings
import net.muskit.questcamstreamer.global.State
import net.muskit.questcamstreamer.stream.RTCClient
import net.muskit.questcamstreamer.video.Camera
import net.muskit.questcamstreamer.video.VideoEncoder
import java.net.URL

class StreamService: LifecycleService() {
    private val TAG = "StreamService"
    private var stopped = false

    private var connectionCoroutine: CoroutineScope? = null
    private var wsClient: ClientWebSocketSession? = null
    private var useCase: ImageAnalysis? = null
    private var notifBuilder: NotificationCompat.Builder? = null

    private var rtcClient: RTCClient? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.END.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        if (State.streamService != null) {
            return
        }
        State.streamService = this

        Log.d(TAG, "start")
        notifBuilder = NotificationCompat.Builder(this, "status_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("You are sharing your camera and microphone.")
            .setOngoing(true)

        setStatus(Status.CONNECTING, null, false)
        startForeground(
            1, notifBuilder!!.build(),
           FOREGROUND_SERVICE_TYPE_MANIFEST
        )

        // websockets
        val http = HttpClient(CIO) {
            install(WebSockets)
        }
        connectionCoroutine = CoroutineScope(Dispatchers.IO).apply {
            launch { runConnection(http)}
        }
    }

    private fun runConnection(http: HttpClient) {
        val url = URL("http://${Settings.connectionString}")
        val host = url.host
        val port = url.port

        val svc = this
        runBlocking {
            try {
                http.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/") {
                    wsClient = this
                    // initialize
                    Log.d(TAG, "tryConnect: initializing webRTC")
                    // TODO: get camera resolution
                    rtcClient = RTCClient(svc) {
                        CoroutineScope(Dispatchers.IO).launch {
                            send(Frame.Text(it))
                        }
                    }
                    rtcClient!!.createOffer()

                    setStatus(Status.CONNECTED)
                    setCam(Settings.streamCam)

                    // listen for control messages
                    try {
                        while (true) {
                            val frame = incoming.receive()
                            if (frame is Frame.Text) {
                                val str = frame.readText()
                                Log.d(TAG, "runConnection: recv: $str")
                                rtcClient!!.recvJson(str)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "runConnection: Error receiving message: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                val msg = "error connecting: ${e.message}"
                Log.e(TAG, msg)
                setStatus(Status.DISCONNECTED, msg)
            }
        }
        stop()
    }

    private fun setStatus(status: Status, errorMessage: String? = null, updateExistingNotif: Boolean = true) {
        State.streamStatus = status
        val i = Intent(CONNECTION_STATUS_CHANGED).putExtra("status", status)
        if (errorMessage != null) {
            i.putExtra("error", errorMessage)
        }
        sendBroadcast(i)
        val txt = status.toString().lowercase().replaceFirstChar { it.uppercaseChar() }
        notifBuilder?.setContentTitle("Status: $txt")

        if (updateExistingNotif) {
            val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notifMan.notify(1, notifBuilder?.build())
        }
    }

    public fun setCam(on: Boolean) {
        val svc = this
        CoroutineScope(Dispatchers.Main).launch{
            if (on && rtcClient != null) {
                useCase = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().apply {
                        setAnalyzer(mainExecutor, object : ImageAnalysis.Analyzer {
                            @ExperimentalGetImage
                            override fun analyze(image: ImageProxy) {
                                val timestamp = System.nanoTime()
                                image.image?.let {
                                    rtcClient?.sendFrame(it, timestamp)
                                }
                                image.close()
                            }
                        })
                        Camera.bindUsecase(svc, this, "stream")
                    }
            } else { // off
                Camera.unbindUsecase("stream")
            }
            Camera.refreshUsecasesLifecycle()
        }
    }

    private fun stop() {
        if (stopped) return
        stopped = true

        Log.d(TAG, "stop")

        // stop connections
        if (connectionCoroutine?.isActive == true) {
            connectionCoroutine?.cancel()
        }
        runBlocking {
            Log.d(TAG, "stop: closing ws")
            wsClient?.close()
        }
        rtcClient?.close()

        // change camera states
        setCam(false)

        // update other states
        State.streamService = null
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
        const val CONNECTION_STATUS_CHANGED = "net.muskit.QuestCamStreamer_CONNECTION_STATUS"
    }
}