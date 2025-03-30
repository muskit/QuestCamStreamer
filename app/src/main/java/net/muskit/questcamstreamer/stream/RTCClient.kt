package net.muskit.questcamstreamer.stream

import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

import android.content.Context
import android.media.Image
import android.util.Log
import net.muskit.questcamstreamer.video.VideoDeliverer
import org.json.JSONObject
import org.webrtc.*

class RTCClient(
    private val context: Context,
    private val senderCallback: (String) -> Unit,
) {
    private val TAG = "RTCClient"

    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnection: PeerConnection?

    private val videoCap: VideoDeliverer

    init {
        val eglBaseContext = EglBase.create().eglBaseContext

        // Initialize WebRTC
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBaseContext,
                    false,
                    true
                )
            )
            .createPeerConnectionFactory()

        // Configure ICE servers
        val iceServers = emptyList<PeerConnection.IceServer>()
//            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
//        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        // initialize video track, which is handled by VideoDeliverer
        videoCap = VideoDeliverer(eglBaseContext)
        val vidTrack = videoCap.initializeVideoTrack(peerConnectionFactory)

        // Create PeerConnection
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
    //                sendJson("candidate", "${iceCandidate.sdpMid},${iceCandidate.sdpMLineIndex},${iceCandidate.sdp}")
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ICE Connection State: $state")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onAddStream(mediaStream: MediaStream) {}
            override fun onRemoveStream(mediaStream: MediaStream) {}
            override fun onDataChannel(dataChannel: DataChannel) {}
            override fun onRenegotiationNeeded() {}
        })?.apply {
            addTrack(vidTrack)
        }
    }

    fun createOffer() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection.setLocalDescription(this, sdp)
                sendJson("offer", sdp.description)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("maxWidth", "2160"))
            mandatory.add(MediaConstraints.KeyValuePair("maxHeight", "2160"))
            mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", "60"))
        })
    }

    fun sendFrame(img: Image, timestampNS: Long) {
//        Log.d(TAG, "sendFrame: res is ${img.width}x${img.height}")
        videoCap.deliverImageFrame(img, 0, timestampNS)
    }

    private fun sendJson(type: String, message: String) {
        val data = mapOf(
            "type" to type,
            "message" to message
        )
        val json = JSONObject(data)
        senderCallback(json.toString())
    }

    fun recvJson(data: String) {
        val j = JSONObject(data)
        val type = j["type"]
        val message = j["message"].toString()
        Log.d(TAG, "handleReceivedJson: [$type]\n$message")

        when(type) {
            "answer" -> {
                val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, message)
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d(TAG, "set remoteDescription successfully") }
                    override fun onSetFailure(error: String?) {
                        Log.d(TAG, "failed to set remoteDescription: $error") }
                    override fun onCreateSuccess(sdp: SessionDescription?) {
                        Log.d(TAG, "created remoteDescription successfully") }
                    override fun onCreateFailure(error: String?) {
                        Log.d(TAG, "failed to create remoteDescription: $error") }
                }, remoteSdp)
            }
            "candidate" -> {
                val parts = message.split(",")
                if (parts.size == 3) {
                    val iceCandidate = IceCandidate(parts[0], parts[1].toInt(), parts[2])
                    peerConnection?.addIceCandidate(iceCandidate)
                }
            }
        }
    }

    fun close() {
        peerConnection?.close()
        peerConnectionFactory.dispose()
    }
}
