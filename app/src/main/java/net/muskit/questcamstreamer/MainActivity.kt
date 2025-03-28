package net.muskit.questcamstreamer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.muskit.questcamstreamer.global.State
import net.muskit.questcamstreamer.ui.MainScreen
import net.muskit.questcamstreamer.ui.theme.QuestCamStreamerTheme
import net.muskit.questcamstreamer.video.Camera

class MainActivity : ComponentActivity() {
    private fun askForPerms() {
        val requestPermissionLauncher =
            registerForActivityResult(RequestMultiplePermissions()) {
                for ((perm, allowed) in it) {
                    Log.d("askForPerm", "$perm granted: $allowed")
                }
            }

        val perms = arrayOf(
            "android.permission.CAMERA",
            "horizonos.permission.HEADSET_CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.INTERNET",

            "android.permission.FOREGROUND_SERVICE",
            // the following are not needed until Android 14 (HorizonOS 74 is on 12)
            "android.permission.FOREGROUND_SERVICE_CAMERA",
            "android.permission.FOREGROUND_SERVICE_MICROPHONE",
//            "android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE"
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
            State.appLifecycleOwner = LocalLifecycleOwner.current
            Camera.initialize(LocalContext.current)
            QuestCamStreamerTheme {
                Scaffold {
                    MainScreen()
                }
            }
        }
    }
}