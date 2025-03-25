package net.muskit.questcamstreamer

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ImageBroadcaster: ImageAnalysis.Analyzer {
    private var timeOfLastFrame: Long = 0
    private val encoder: VideoEncoder

    constructor() {
        // TODO: get actual resolution
        encoder = VideoEncoder(1280, 960)
    }

    override fun analyze(image: ImageProxy) {
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