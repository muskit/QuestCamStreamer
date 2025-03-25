package net.muskit.questcamstreamer

import androidx.lifecycle.LifecycleOwner

object State {
    var broadcastService: BroadcastService? = null
    lateinit var appLifecycleOwner: LifecycleOwner
}