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
        set(v) { field = v; save() }
    var rightCam = false
        set(v) { field = v; save() }
    var camPreview = true
        set(v) { field = v; save() }

    var broadcastMic = true
        set(v) { field = v; save() }

    override fun toString(): String {
        return "broadcastCam=${State.broadcastCam}; rightCam=${State.rightCam}"
    }

    public fun save() {
        Log.d("States", "saveState: $this")
    }

    public fun load() {
        Log.d("States", "loadState: $this")
    }
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