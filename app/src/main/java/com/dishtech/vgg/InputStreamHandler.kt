package com.dishtech.vgg

import android.content.Context
import android.content.res.AssetManager

class AssetInputStreamHandler private constructor(private val inputStream:
                                                  AssetManager.AssetInputStream) {
    companion object {
        fun streamHandler(context: Context, resource: Int) =
            AssetInputStreamHandler(context.resources.openRawResource(resource)
                                            as AssetManager.AssetInputStream)
    }

    private var currentStreamOffset = 0u;

    private var mark = 0u

    fun markCurrent() {
        mark = currentStreamOffset
        inputStream.mark(inputStream.available())
    }

    fun reset() {
        currentStreamOffset = mark
        inputStream.reset()
    }

    fun read(byteArray: ByteArray) = read(byteArray, 0, byteArray.size)

    fun read(byteArray: ByteArray, offset: Int, len: Int) : Int {
        val res = inputStream.read(byteArray, offset, len)
        currentStreamOffset += res.toUInt()
        return res
    }

    /**
     * Reads the bytes at offset 'offset' from the file start into 'byteArray'.
     */
    fun readAtOffset(byteArray: ByteArray, offset: UInt) : Int{
        val readAmount = byteArray.size.toUInt()
        val available = available()
        val uAvailable = if (available >= 0) available.toUInt() else 0u
        if (offset < mark || offset + readAmount - currentStreamOffset > uAvailable) {
            throw  RuntimeException("Tried to read unavailable data from input streamer.")
        }
        if (offset < currentStreamOffset) {
            reset()
        }
        if (offset != currentStreamOffset) {
            inputStream.skip((offset - currentStreamOffset).toLong())
        }
        currentStreamOffset = offset
        return read(byteArray)
    }

    fun available() = inputStream.available()

    fun markSupported() = inputStream.markSupported()

    fun close() = inputStream.close()

}