package net.muskit.questcamstreamer.video
import android.media.Image
import org.webrtc.*

class VideoDeliverer(private val eglBaseContext: EglBase.Context) {
    private val TAG = "VideoDeliverer"

    private lateinit var videoSource: VideoSource
    private lateinit var surfaceTextureHelper: SurfaceTextureHelper

    fun initializeVideoTrack(factory: PeerConnectionFactory): VideoTrack {
        // Create SurfaceTextureHelper (required for WebRTC to handle textures)
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)

        // Create VideoSource
        videoSource = factory.createVideoSource(false)

        // Create VideoTrack from the VideoSource
        return factory.createVideoTrack("videoTrack", videoSource)
    }

    fun deliverImageFrame(image: Image, rotation: Int, timestampNs: Long) {
        val videoFrame = yuv420888ToI420Frame(image, rotation, timestampNs)
        deliverFrame(videoFrame)
    }

    private fun deliverFrame(frame: VideoFrame) {
//        Log.d(TAG, "deliverFrame: res is ${frame.rotatedWidth}x${frame.rotatedHeight}")
        videoSource.capturerObserver.onFrameCaptured(frame)
    }

    fun yuv420888ToI420Frame(image: Image, rotation: Int, timestampNs: Long): VideoFrame {
        val i420Buffer = imageToI420Buffer(image)
        return VideoFrame(i420Buffer, rotation, timestampNs)
    }
}
