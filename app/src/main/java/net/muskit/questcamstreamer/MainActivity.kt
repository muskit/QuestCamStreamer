package net.muskit.questcamstreamer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import net.muskit.questcamstreamer.ui.MainScreen
import net.muskit.questcamstreamer.ui.theme.QuestCamStreamerTheme

class MainActivity : ComponentActivity() {
    private fun askForPerms() {
        val requestPermissionLauncher =
            registerForActivityResult(RequestMultiplePermissions()) {
                perms: Map<String, Boolean> ->
                    for ((perm, allowed) in perms) {
                        Log.d("askForPerm", "$perm granted: $allowed")
                    }
            }

        val perms = arrayOf(
            "android.permission.CAMERA",
            "horizonos.permission.HEADSET_CAMERA",
            "android.permission.RECORD_AUDIO"
        )
        Log.d("askForPerm", "asking for ${perms.size} permissions...")
        requestPermissionLauncher.launch(perms)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // get camera & mic access
        askForPerms()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuestCamStreamerTheme {
                Scaffold {
                    MainScreen()
                }
            }
        }
    }
}