package net.muskit.questcamstreamer.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.muskit.questcamstreamer.ui.icons.QrCodeScan
import net.muskit.questcamstreamer.ui.theme.QuestCamStreamerTheme

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
    var connText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = connText,
            onValueChange = { connText = it },
            label = { Text("host:port") },
            singleLine = true,
            trailingIcon = {
                IconButton(
                    content = {
                        Icon(QrCodeScan, contentDescription = null)
                    },
                onClick = {
                    Log.d("UI", "ConnectionPane: QR scan clicked!")
                })
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                Log.d("UI", "ConnectionPane: Connect click! to $connText")
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Connect")
                Spacer(modifier = Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "")
            }
        }
    }
}


