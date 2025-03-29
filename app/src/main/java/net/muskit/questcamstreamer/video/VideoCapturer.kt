package net.muskit.questcamstreamer.video
import android.media.Image
import android.util.Log
import org.webrtc.*

class VideoCapturer(private val eglBaseContext: EglBase.Context) {
    private lateinit var videoSource: VideoSource
    private lateinit var surfaceTextureHelper: SurfaceTextureHelper
    private lateinit var rtcVideoTrack: VideoTrack

    fun initialize(factory: PeerConnectionFactory) {
        // Create SurfaceTextureHelper (required for WebRTC to handle textures)
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)

        // Create VideoSource
        videoSource = factory.createVideoSource(false)

        // Create VideoTrack from the VideoSource
        rtcVideoTrack = factory.createVideoTrack("videoTrack", videoSource)
    }

    fun deliverFrame(frame: VideoFrame) {
        videoSource.capturerObserver.onFrameCaptured(frame)
    }

    fun deliverImageFrame(image: Image, rotation: Int, timestampNs: Long) {
        Log.d("VideoCapturer", "deliverImageFrame: $timestampNs")
        // Convert Image to WebRTC's format (e.g., NV21 or I420 buffer)
        val yuvBuffer = convertImageToYUV(image)  // You need to implement this

        // Wrap in WebRTC VideoFrame
        val videoFrame = VideoFrame(yuvBuffer, rotation, timestampNs)

        // Push frame into WebRTC
        deliverFrame(videoFrame)
    }

    private fun convertImageToYUV(image: Image): VideoFrame.Buffer {
        // Convert Android Image (YUV_420_888) to I420 or NV12 format
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val width = image.width
        val height = image.height
        val strideY = image.planes[0].rowStride
        val strideU = image.planes[1].rowStride
        val strideV = image.planes[2].rowStride

        return JavaI420Buffer.wrap(width, height, yBuffer, strideY, uBuffer, strideU, vBuffer, strideV, null)
    }
}
