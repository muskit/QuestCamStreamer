package net.muskit.questcamstreamer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.muskit.questcamstreamer.StreamService
import net.muskit.questcamstreamer.global.Settings
import net.muskit.questcamstreamer.global.State
import net.muskit.questcamstreamer.ui.icons.QrCodeScan
import net.muskit.questcamstreamer.ui.theme.QuestCamStreamerTheme
import java.net.URL

@Composable
@Preview(showBackground = true)
fun ConnPanePreview() {
    QuestCamStreamerTheme {
        ConnectionPane()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionPane(modifier: Modifier = Modifier) {
    var connText by remember { mutableStateOf(Settings.connectionString) }
    var status by remember { mutableStateOf(State.streamStatus) }
    val ctx = LocalContext.current

    DisposableEffect(ctx) {
        val intentFilter = IntentFilter(StreamService.CONNECTION_STATUS_CHANGED)

        val receiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                status = intent?.extras?.get("status") as StreamService.Status
                Log.d("UI", "ConnectionPane: got status $status")
            }
        }

        // Old way, may crash in Android 14
        // context.registerReceiver(receiver, intentFilter)

        // Recommended way, checks Android API compatibility for you
        ContextCompat.registerReceiver(
            ctx,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            ctx.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = connText,
            onValueChange = {
                connText = it;
                Settings.connectionString = it
            },
            label = { Text("host:port") },
            singleLine = true,
            enabled = (status == StreamService.Status.DISCONNECTED),
            trailingIcon = {
                IconButton(
                    content = {
                        Icon(QrCodeScan, contentDescription = null)
                    },
                onClick = {
                    Log.d("UI", "ConnectionPane: QR scan clicked!")
                    Toast.makeText(ctx, "QR Scan coming soon!", Toast.LENGTH_SHORT).show()
                })
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        key(connText, status) {
            // validate connectionString to set button state
            var host = ""
            var port = -1
            try {
                val url = URL("http://${connText}")
                host = url.host
                port = url.port
            } catch (e: Exception) {
                Log.e("UI", "ConnectionPane: could not create URL: $e", )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonDefaults.MinHeight),
                onClick = {
                    Log.d("UI", "ConnectionPane: Connect click! to $connText")

                    Intent(ctx, StreamService::class.java).also {
                        it.action = when(status) {
                            StreamService.Status.DISCONNECTED -> StreamService.Actions.START.toString()
                            else -> StreamService.Actions.END.toString()
                        }
                        Log.d("UI", "ConnectionPane: launching intent $it")
                        ctx.startService(it)
                    }
                },
                enabled = status != StreamService.Status.CONNECTING
            ) {
                when(status) {
                    StreamService.Status.CONNECTED -> Text("Disconnect")
                    StreamService.Status.DISCONNECTED -> Text("Connect")
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connecting...")
                        }
                    }
                }
            }
        }
    }
}


