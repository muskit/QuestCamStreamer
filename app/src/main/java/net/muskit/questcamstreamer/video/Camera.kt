package net.muskit.questcamstreamer.video

import android.content.Context
import android.util.Log
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import net.muskit.questcamstreamer.global.Settings
import net.muskit.questcamstreamer.global.State

object Camera {
    public lateinit var camera: androidx.camera.core.Camera
        private set

    private lateinit var appContext: Context
    private lateinit var camProviderFut: ListenableFuture<ProcessCameraProvider>
    private val useCases = mutableMapOf<String, UseCase>()

    public fun initialize(ctx: Context) {
        appContext = ctx
        if (!Camera::camProviderFut.isInitialized)
            camProviderFut = ProcessCameraProvider.getInstance(ctx)
    }

    public fun bindUsecase(useCase: UseCase, from: String) {
        camProviderFut.addListener({
            val cameraProvider = camProviderFut.get()

            if (useCases.containsKey(from))
                cameraProvider.unbind(useCases[from])

            useCases[from] = useCase
            refreshUsecasesLifecycle()
        }, appContext.mainExecutor)
    }

    public fun unbindUsecase(from: String) {
        if (useCases.containsKey(from)) {
            camProviderFut.addListener({
                val cameraProvider = camProviderFut.get()
                cameraProvider.unbind(useCases[from])
                useCases.remove(from)
            }, appContext.mainExecutor)
        }
    }

    public fun refreshUsecasesLifecycle() {
        camProviderFut.addListener ({
            val cameraProvider = camProviderFut.get()
            cameraProvider.unbindAll()

            val lifecycle = State.streamService ?: State.appLifecycleOwner
            if (lifecycle == State.streamService)
                Log.d("Camera", "refreshUsecasesLifecycle: using streamer lifecycle")
            else
                Log.d("Camera", "refreshUsecasesLifecycle: using app lifecycle")

            for ((_, u) in useCases) {
                camera = cameraProvider.bindToLifecycle(lifecycle, Settings.camSelector(cameraProvider), u)
            }
        }, getMainExecutor(appContext))
    }
}