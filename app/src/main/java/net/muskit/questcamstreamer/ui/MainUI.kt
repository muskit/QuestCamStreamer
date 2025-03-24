package net.muskit.questcamstreamer.ui

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import net.muskit.questcamstreamer.Camera
import net.muskit.questcamstreamer.ImageBroadcaster
import net.muskit.questcamstreamer.State
import net.muskit.questcamstreamer.camSelectorFromState
import net.muskit.questcamstreamer.ui.theme.QuestCamStreamerTheme

val innerPadding = 12.dp

@Preview(showBackground = true)
@Composable
fun AppPreviewPortrait() {
    QuestCamStreamerTheme {
        MainScreen(true)
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun AppPreviewLandscape() {
    QuestCamStreamerTheme {
        MainScreen(true)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(idePreview: Boolean = false) {
    var camEnabled by remember { mutableStateOf(State.broadcastCam) }
    var micEnabled by remember { mutableStateOf(State.broadcastMic) }

    Column(modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f),
        ) {
            if (idePreview) {
                Spacer(modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Cyan)
                )
            } else {
                val ctx = LocalContext.current
                val analyzer = remember {
                    ImageBroadcaster()
                }
                val controller = remember {
                    LifecycleCameraController(ctx).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(ctx),
                            analyzer
                        )
                        Camera.controller = this

                        val camProviderFut = ProcessCameraProvider.getInstance(ctx)
                        camProviderFut.addListener({
                            Camera.controller!!.cameraSelector = camSelectorFromState(ctx)
                        }, ctx.mainExecutor)
                    }
                }
                CameraPreview(
                    controller = controller,
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight()
                )
            }
        }
        FlowRow {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(innerPadding)
            ) {
                Text("Connect", fontSize = 36.sp)
                ConnectionPane()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(innerPadding)
            ) {
                Text("Video", fontSize = 36.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enabled", fontSize = 20.sp)
                    Switch(
                        checked = camEnabled,
                        onCheckedChange = { state ->
                            camEnabled = state
                            State.broadcastCam = state
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(innerPadding)

            ) {
                // TODO: audio settings
                Text("Audio", fontSize = 36.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enabled", fontSize = 20.sp)
                    Switch(
                        checked = micEnabled,
                        onCheckedChange = { state ->
                            micEnabled = state
                            State.broadcastMic = state
                        }
                    )
                }
            }
        }
    }
}