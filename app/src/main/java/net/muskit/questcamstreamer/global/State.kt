package net.muskit.questcamstreamer.global

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import net.muskit.questcamstreamer.StreamService

object State {
    var streamService: StreamService? = null
    lateinit var appLifecycleOwner: LifecycleOwner
}