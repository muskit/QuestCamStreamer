package net.muskit.questcamstreamer

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController

object State {
    var broadcastCam = true
        set(v) { field = v; saveState() }
    var rightCam = false
        set(v) { field = v; saveState() }
    var camPreview = true
        set(v) { field = v; saveState() }

    var broadcastMic = true
        set(v) { field = v; saveState() }

}

object Camera {
    var controller: LifecycleCameraController? = null

    public fun setCamera(ctx: Context) {
        if (controller == null) return

        val selector = camSelectorFromState(ctx)
        controller!!.cameraSelector = selector
    }
}

fun camFilterFromState(cameraProvider: ProcessCameraProvider, cameraInfo: CameraInfo): Boolean {
    if (State.rightCam)
        return cameraInfo == cameraProvider.availableCameraInfos.last()
    return cameraInfo == cameraProvider.availableCameraInfos.first()
}

fun camSelectorFromState(ctx: Context): CameraSelector {
    val camProvider = ProcessCameraProvider.getInstance(ctx).get()
    val selector = CameraSelector.Builder()

    selector.addCameraFilter {
        it.filter{
            camFilterFromState(camProvider, it)
        }
    }

    return selector.build()
}

fun saveState() {
    Log.d("States", "saveState: broadcastCam=${State.broadcastCam}; rightCam=${State.rightCam}")
}

fun loadState() {

}