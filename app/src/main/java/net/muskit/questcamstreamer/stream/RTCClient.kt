package net.muskit.questcamstreamer.stream

import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

import android.content.Context
import android.media.Image
import android.util.Log
import net.muskit.questcamstreamer.video.VideoCapturer
import org.webrtc.*
import java.net.Socket

class RTCClient(
    private val context: Context,
    private val socket: Socket, // Already established connection
    private val eglBase: EglBase = EglBase.create()
) {
    private val TAG = "RTCClient"

    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnection: PeerConnection?

    private val videoCap: VideoCapturer

    init {
        val eglBaseContext = eglBase.eglBaseContext

        // Initialize WebRTC
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .createPeerConnectionFactory()

        // Configure ICE servers
        val iceServers = listOf<PeerConnection.IceServer>(
//            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        // initialize capturer, set up video stream
        videoCap = VideoCapturer(eglBaseContext)
        videoCap.initialize(peerConnectionFactory)

        // Create PeerConnection
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                sendMessage("ICE:${iceCandidate.sdpMid},${iceCandidate.sdpMLineIndex},${iceCandidate.sdp}")
            }


            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                println("ICE Connection State: $state")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onAddStream(mediaStream: MediaStream) {}
            override fun onRemoveStream(mediaStream: MediaStream) {}
            override fun onDataChannel(dataChannel: DataChannel) {}
            override fun onRenegotiationNeeded() {}
        })
    }

    fun createOffer() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection.setLocalDescription(this, sdp)
                sendMessage("OFFER:${sdp.description}")
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    fun handleReceivedMessage(message: String) {
        Log.d(TAG, "handleReceivedMessage: $message")
        when {
            message.startsWith("OFFER:") -> {
                val sdp = message.removePrefix("OFFER:")
                val remoteSdp = SessionDescription(SessionDescription.Type.OFFER, sdp)
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        peerConnection.createAnswer(object : SdpObserver {
                            override fun onCreateSuccess(sdp: SessionDescription) {
                                peerConnection.setLocalDescription(this, sdp)
                                sendMessage("ANSWER:${sdp.description}")
                            }

                            override fun onSetSuccess() {}
                            override fun onCreateFailure(error: String?) {}
                            override fun onSetFailure(error: String?) {}
                        }, MediaConstraints())
                    }

                    override fun onSetFailure(error: String?) {}
                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                }, remoteSdp)
            }
            message.startsWith("ANSWER:") -> {
                val sdp = message.removePrefix("ANSWER:")
                val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, sdp)
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {}
                    override fun onSetFailure(error: String?) {}
                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                }, remoteSdp)
            }
            message.startsWith("ICE:") -> {
                val parts = message.removePrefix("ICE:").split(",")
                if (parts.size == 3) {
                    val iceCandidate = IceCandidate(parts[0], parts[1].toInt(), parts[2])
                    peerConnection?.addIceCandidate(iceCandidate)
                }
            }
        }
    }

    fun sendFrame(img: Image, timestampNS: Long) {
        videoCap.deliverImageFrame(img, 0, timestampNS)
    }

    private fun sendMessage(message: String) {
        socket.getOutputStream().write((message + "\n").toByteArray())
    }

    fun close() {
        peerConnection?.close()
        peerConnectionFactory.dispose()
        socket.close()
    }
}
