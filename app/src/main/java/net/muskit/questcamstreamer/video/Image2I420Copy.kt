package net.muskit.questcamstreamer.video

import android.media.Image
import org.webrtc.JavaI420Buffer
import java.nio.ByteBuffer

// Returns an I420Buffer consisting Image's copied plane buffers.
// Allows for color preservation when WebRTC scales the sent data
fun imageToI420Buffer(image: Image): JavaI420Buffer {
    val width = image.width
    val height = image.height

    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yStride = yPlane.rowStride
    val uStride = uPlane.rowStride
    val vStride = vPlane.rowStride
    val uPixelStride = uPlane.pixelStride
    val vPixelStride = vPlane.pixelStride

    // Explicitly allocate a properly aligned I420 buffer
    val i420Buffer = JavaI420Buffer.allocate(width, height)

    // Copy Y plane
    copyPlane(yPlane.buffer, i420Buffer.dataY, width, height, yStride)

    // Check if the U/V planes need swapping (NV21 case)
    val isNV21 = uPixelStride == 2 && vPixelStride == 2

    // Copy U and V planes in the correct order
    if (isNV21) {
        copyUVPlane(vPlane.buffer, i420Buffer.dataU, width / 2, height / 2, vStride, vPixelStride)
        copyUVPlane(uPlane.buffer, i420Buffer.dataV, width / 2, height / 2, uStride, uPixelStride)
    } else {
        copyUVPlane(uPlane.buffer, i420Buffer.dataU, width / 2, height / 2, uStride, uPixelStride)
        copyUVPlane(vPlane.buffer, i420Buffer.dataV, width / 2, height / 2, vStride, vPixelStride)
    }

    return i420Buffer
}

// Helper function to copy the Y plane
private fun copyPlane(source: ByteBuffer, destination: ByteBuffer, width: Int, height: Int, rowStride: Int) {
    val rowBuffer = ByteArray(width) // Temporary buffer to store each row

    for (row in 0 until height) {
        val position = row * rowStride
        source.position(position) // Move to the start of the row
        source.get(rowBuffer, 0, width) // Read only the valid width
        destination.put(rowBuffer) // Copy into destination buffer
    }
}

// Helper function to copy UV planes
private fun copyUVPlane(source: ByteBuffer, destination: ByteBuffer, width: Int, height: Int, rowStride: Int, pixelStride: Int) {
    for (row in 0 until height) {
        for (col in 0 until width) {
            val position = row * rowStride + col * pixelStride
            destination.put(source.get(position))
        }
    }
}
