package net.muskit.questcamstreamer.video

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import net.muskit.questcamstreamer.stream.RTCClient

class ImageStreamer(
    private val rtcClient: RTCClient
): ImageAnalysis.Analyzer {
    private var timeOfLastFrame: Long = 0
    private lateinit var encoder: VideoEncoder

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val timestamp = System.nanoTime()
        image.image?.let {
            rtcClient.sendFrame(it, timestamp)
        }
        image.close()
        return

        /////// previous plan involving our custom encoder ///////
        if (!::encoder.isInitialized || encoder.width != image.width || encoder.height != image.height) {
            encoder = VideoEncoder(image.width, image.height)
        }

        // FPS display
        var frameTime = System.currentTimeMillis() - timeOfLastFrame
        timeOfLastFrame = System.currentTimeMillis()
        Log.d("analyze", "FPS: ${1000.0/frameTime}")


        // send frame
        encoder.processFrame(image.toBitmap()) {
            // TODO: send encoded frame data
//            Log.d("processFrame", "frame data is ${it.size} bytes")
        }
        image.close()
    }
}