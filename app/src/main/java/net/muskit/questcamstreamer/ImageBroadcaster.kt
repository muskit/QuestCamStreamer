package net.muskit.questcamstreamer

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ImageBroadcaster: ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {

        image.close()
    }

}