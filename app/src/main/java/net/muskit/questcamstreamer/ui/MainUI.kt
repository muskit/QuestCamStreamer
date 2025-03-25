package net.muskit.questcamstreamer.ui

import android.util.AttributeSet
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import net.muskit.questcamstreamer.Camera
import net.muskit.questcamstreamer.Settings
import net.muskit.questcamstreamer.State
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
    var camEnabled by remember { mutableStateOf(Settings.broadcastCam) }
    var micEnabled by remember { mutableStateOf(Settings.broadcastMic) }
    var rightCam by remember { mutableStateOf(Settings.rightCam) }

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
                CameraPreview(
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
                            Settings.broadcastCam = state
                            State.broadcastService?.setCam(state)
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Eye", fontSize = 20.sp)
                    Button(
                        onClick = {
                            Settings.rightCam = !Settings.rightCam
                            rightCam = Settings.rightCam
                            Camera.refreshUsecasesLifecycle()
                        },
                        content = {
                            Row {
                                val leftWeight = when(rightCam) {
                                    true -> FontWeight.Light
                                    else -> FontWeight.Black
                                }
                                val rightWeight = when(rightCam) {
                                    true -> FontWeight.Black
                                    else -> FontWeight.Light
                                }
                                Text("L", fontSize = 16.sp, fontWeight = leftWeight)
                                Spacer(modifier = Modifier.width(30.dp))
                                Text("R", fontSize = 16.sp, fontWeight = rightWeight)
                            }
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
                            Settings.broadcastMic = state
                        }
                    )
                }
            }
        }
    }
}