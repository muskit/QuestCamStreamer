package net.muskit.questcamstreamer.global

import android.util.Log
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider

object Settings {
    var streamCam = true
        set(v) { field = v; save() }
    var rightCam = false
        set(v) { field = v; save() }
    var camPreview = true
        set(v) { field = v; save() }

    var streamMic = true
        set(v) { field = v; save() }

    var connectionString = "192.168.50.143:12345"
        set(v) { field = v; save() }

    public fun camFilter(cameraProvider: ProcessCameraProvider, cameraInfo: CameraInfo): Boolean {
        if (rightCam)
            return cameraInfo == cameraProvider.availableCameraInfos.last()
        return cameraInfo == cameraProvider.availableCameraInfos.first()
    }

    public fun camSelector(cameraProvider: ProcessCameraProvider): CameraSelector {
        val selector = CameraSelector.Builder()

        selector.addCameraFilter {
            it.filter{
                camFilter(cameraProvider, it)
            }
        }

        return selector.build()
    }

    override fun toString(): String {
        return "streamCam=$streamCam; rightCam=$rightCam"
    }

    public fun save() {
        Log.d("Settings", "save: $this")
    }

    public fun load() {
        Log.d("Settings", "load: $this")
    }
}