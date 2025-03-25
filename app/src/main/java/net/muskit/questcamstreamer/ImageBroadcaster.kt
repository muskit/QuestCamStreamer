package net.muskit.questcamstreamer

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ImageBroadcaster: ImageAnalysis.Analyzer {
    private var timeOfLastFrame: Long = 0
    private lateinit var encoder: VideoEncoder

    override fun analyze(image: ImageProxy) {
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