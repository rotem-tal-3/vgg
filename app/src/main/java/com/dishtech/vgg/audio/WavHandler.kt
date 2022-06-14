package com.dishtech.vgg.audio

import com.dishtech.vgg.AssetInputStreamHandler
import com.dishtech.vgg.MathHelper
import com.dishtech.vgg.quadrenderer.ValueFeeder
import org.koin.core.component.KoinComponent
import kotlin.math.absoluteValue

class WavHeader (
    val chunkID : ByteArray,
    val chunkSize: UInt,
    val format: ByteArray,
    val subChunk1ID: ByteArray,
    val subChunk1Size: UInt,
    val audioFormat: Short,
    val numChannels: Short,
    val sampleRate: UInt,
    val byteRate: UInt,
    val blockAlign: Short,
    val bitsPerSample: Short,
    val subChunk2ID: ByteArray,
    val subChunk2Size: UInt
)

class Spectrum(val spectrum: FloatArray, val bandWidth: Float)

class SpectrumFeeder(private val wavHandler: WavHandler) : ValueFeeder<Spectrum> {
    override fun valueForTime(timeInSeconds: Float): Spectrum {
        return Spectrum(wavHandler.valueForTime(timeInSeconds), wavHandler.bandWidth())
    }

    override fun duration() = wavHandler.duration()
}

class WavHandler(private val inputStream: AssetInputStreamHandler) : ValueFeeder<FloatArray>,
                                                                     KoinComponent {
    companion object {
        private const val BYTE_SIZE = Byte.SIZE_BITS
        private const val INVALID = -1

        // The display time of a frame in 50 FPS.
        private const val ESTIMATED_FRAME_TIME = 0.04545f

         // Size for the wave header
        private const val HEADER_SIZE = 44

        /**
         * Extracts an int starting from index "start" of "buffer"
         *
         * @param start  start position of the int inside the buffer
         * @param buffer Buffer holding the information
         * @return converted number
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        private fun toInt(start: Int, buffer: UByteArray): UInt {
            val intStart = start + 3
            val k = -1
            return (buffer[intStart].toUInt() shl (BYTE_SIZE * 3)) +
                    (buffer[intStart + k * 1].toUInt() shl (BYTE_SIZE * 2)) +
                    (buffer[intStart + k * 2].toUInt() shl BYTE_SIZE) +
                    buffer[intStart + k * 3].toUInt()
        }

        /**
         * Extracts a short starting from index "start" of "buffer"
         *
         * @param start  start position of the short inside the buffer
         * @param buffer Buffer holding the information
         * @return converted number
         */
        private fun toShort(start: Int, buffer: ByteArray): Short {
            val shortStart = start + 1
            val k = -1
            return ((buffer[shortStart].toInt() shl BYTE_SIZE) +
                    buffer[shortStart + k * 1]).toShort()
        }
    }

    private fun createWavHeader(): WavHeader {
        val buf = ByteArray(HEADER_SIZE)
        val res = inputStream.read(buf, 0, HEADER_SIZE);
        inputStream.markCurrent()
        if (res != HEADER_SIZE) {
            throw RuntimeException("Could not read WAV header.");
        }
        val chunkID = buf.copyOfRange(0, 4)
        if (String(chunkID).compareTo("RIFF") != 0) {
            throw RuntimeException("Illegal WAV format.");
        }
        val ubuf = buf.asUByteArray()

        return WavHeader(chunkID,
                         toInt(4, ubuf),
                         buf.copyOfRange(8, 12),
                         buf.copyOfRange(12, 16),
                         toInt(16, ubuf), toShort(20, buf),
                         toShort(22, buf), toInt(24, ubuf),
                         toInt(28, ubuf), toShort(32, buf),
                         toShort(34, buf),
                         buf.copyOfRange(36, 40),
                         toInt(40, ubuf))
    }

    // Object containing data extracted from the input WAV header.
    private val header = createWavHeader()

    // Duration of the wav in seconds.
    private val wavDuration = header.subChunk2Size.toFloat() / header.byteRate.toFloat()

    // Size in bytes of the time window fed to the FFT
    private val timeSize = MathHelper.nearestPowerOfTwo(header.byteRate.toFloat() *
                                                                ESTIMATED_FRAME_TIME)

    // Object performing FFT transform on data from the WAV file.
//    private val fft:FFT by inject() { parametersOf(timeSize, header.sampleRate.toFloat()) }
    private val fft = FFT(timeSize, header.sampleRate.toFloat())

    override fun duration(): Float {
        return wavDuration
    }

    override fun valueForTime(timeInSeconds: Float) : FloatArray {
        val offset = offsetForTime(timeInSeconds)
//        if (offset + timeSize.toUInt() > inputStream.available().toUInt()) {
//            return FloatArray(fft.specSize())
//        }
        performForward(offset)
        return fft.getSpectrum().clone()
    }

    fun bandWidth() = fft.bandWidth.absoluteValue

    private fun performForward(offset: UInt) {
        val buffer = ByteArray(timeSize)
        if (inputStream.readAtOffset(buffer, offset) != timeSize) {
            throw RuntimeException("Could not fully read input stream")
        }
        fft.forward(buffer.map { it.toFloat() }.toFloatArray())
    }

    private fun offsetForTime(timeInSeconds: Float) : UInt {
        val dataOffset =  (timeInSeconds.toDouble() * header.byteRate.toLong()).toUInt()
        val utimeSize = timeSize.toUInt()
        val offsetWithStride = (dataOffset / utimeSize) * utimeSize
        if (offsetWithStride + utimeSize >= header.subChunk2Size) {
            return header.subChunk2Size - utimeSize
        }
        return HEADER_SIZE.toUInt() + offsetWithStride
    }
}