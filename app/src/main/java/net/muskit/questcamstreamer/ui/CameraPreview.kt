package net.muskit.questcamstreamer.ui

import android.util.Log
import android.view.View
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ActionProvider.VisibilityListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.muskit.questcamstreamer.video.Camera
import net.muskit.questcamstreamer.global.Settings
import net.muskit.questcamstreamer.ui.icons.PreviewOff
import net.muskit.questcamstreamer.ui.icons.PreviewOn
import net.muskit.questcamstreamer.ui.theme.QuestCamStreamerTheme

@Preview(showBackground = true)
@Composable
fun Preview() {
    QuestCamStreamerTheme {
//        CameraPreview(LocalContext.current)
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier
) {
    @Composable
    fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }

    val lifecycleOwner = LocalLifecycleOwner.current
    var showPreview by remember { mutableStateOf(Settings.camPreview) }
    val previewUsecase = androidx.camera.core.Preview.Builder()
        .build()

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        Camera.unbindUsecase("preview")
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (showPreview)
            Camera.bindUsecase(previewUsecase, "preview")
    }

    Box(modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        key(showPreview) {
            AndroidView(
                factory = {
                    if (showPreview) {
                        PreviewView(it).apply {
                            scaleType = PreviewView.ScaleType.FIT_CENTER
                            previewUsecase.surfaceProvider = this.surfaceProvider
                            Camera.bindUsecase(previewUsecase, "preview")
                        }
                    } else {
                        View(it)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .absolutePadding(right = 3.dp),
            onClick = {
                showPreview = !showPreview
                Log.d("UI", "CameraPreview: toggled preview! state is now $showPreview")
                if (showPreview)
                    Camera.bindUsecase(previewUsecase, "preview")
                else
                    Camera.unbindUsecase("preview")
            }
        ) {
            val icon: () -> ImageVector = {
                if (showPreview) {
                    PreviewOn
                } else {
                    PreviewOff
                }
            }
            Icon(imageVector = icon(), contentDescription = null)
        }
    }

    VisibilityListener {
        Log.d("UI", "CameraPreview: visibility = $it")
    }
}