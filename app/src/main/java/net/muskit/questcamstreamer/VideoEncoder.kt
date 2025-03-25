package net.muskit.questcamstreamer

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat

class VideoEncoder {
    private val encoder: MediaCodec

    constructor(width: Int, height: Int) {
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000) // 2 mbps
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
    }

    public fun processFrame(bitmap: Bitmap, frameHandler: (ByteArray) -> Unit) {
        val inputIndex = encoder.dequeueInputBuffer(10000)
        if (inputIndex >= 0) {
            val inputBuffer = encoder.getInputBuffer(inputIndex)!!
            val yuvData = bitmapToNV21(bitmap)

            inputBuffer.clear()
            inputBuffer.put(yuvData)
            encoder.queueInputBuffer(inputIndex, 0, yuvData.size, System.nanoTime() / 1000, 0)
        }

        // Get and handle encoded data
        val bufferInfo = MediaCodec.BufferInfo()
        var outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        while (outputIndex >= 0) {
            val outputBuffer = encoder.getOutputBuffer(outputIndex)!!
            val encodedData = ByteArray(bufferInfo.size)
            outputBuffer.get(encodedData)

            frameHandler(encodedData)

            encoder.releaseOutputBuffer(outputIndex, false)
            outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
        }
    }

    private fun bitmapToNV21(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val yuv = ByteArray(width * height * 3 / 2)

        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val color = argb[j * width + i]
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF

                val y = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                val u = (-0.14713 * r - 0.28886 * g + 0.436 * b).toInt() + 128
                val v = (0.615 * r - 0.51498 * g - 0.10001 * b).toInt() + 128

                yuv[yIndex++] = y.toByte()
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv[uvIndex++] = v.toByte()
                    yuv[uvIndex++] = u.toByte()
                }
            }
        }
        return yuv
    }
}