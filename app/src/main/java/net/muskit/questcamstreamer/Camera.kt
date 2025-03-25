package net.muskit.questcamstreamer

import android.content.Context
import android.util.Log
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture

object Camera {
    public lateinit var camera: androidx.camera.core.Camera
        private set

    private lateinit var appContext: Context
    private lateinit var camProviderFut: ListenableFuture<ProcessCameraProvider>
    private val useCases = mutableMapOf<String, UseCase>()

    public fun initialize(ctx: Context) {
        appContext = ctx
        if (!::camProviderFut.isInitialized)
            camProviderFut = ProcessCameraProvider.getInstance(ctx)
    }

    public fun bindUsecase(lifecycleOwner: LifecycleOwner, useCase: UseCase, from: String) {
        // wait for provider if it's not ready yet
        if (!camProviderFut.isDone) {
            camProviderFut.addListener({
                bindUsecase(lifecycleOwner, useCase, from)
            }, appContext.mainExecutor)
            return
        }

        val cameraProvider = camProviderFut.get()

        if (useCases.containsKey(from))
            cameraProvider.unbind(useCases[from])

        useCases[from] = useCase
        refreshUsecasesLifecycle()
    }

    public fun unbindUsecase(from: String) {
        if (useCases.containsKey(from)) {
            val cameraProvider = camProviderFut.get()
            cameraProvider.unbind(useCases[from])
            useCases.remove(from)
        }
    }

    public fun refreshUsecasesLifecycle() {
        val cameraProvider = camProviderFut.get()

        cameraProvider.unbindAll()

        val lifecycle = State.broadcastService ?: State.appLifecycleOwner
        if (lifecycle == State.broadcastService)
            Log.d("Camera", "refreshUsecasesLifecycle: using broadcast lifecycle")
        else
            Log.d("Camera", "refreshUsecasesLifecycle: using app lifecycle")

        for ((_, u) in useCases) {
            camera = cameraProvider.bindToLifecycle(lifecycle, Settings.camSelector(cameraProvider), u)
        }
    }
}