package com.dishtech.vgg.shaders

import android.opengl.GLES30
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureBuffer(override val uniform: Uniform, override val location: Int,
                    override val channel: Int) : GLTextureResource {
    override var needsSet
        get() = false
        set(value) {
            if (!value) { return }
            setData(uniform.value as FloatArray)
        }
    private val buffer: FloatBuffer
    val bufferID: Int
    override val id: Int
    private val dataSize: Int

    init {
        assert(uniform.value is FloatArray)
        bufferID = generateBufferID()
        val data = uniform.value as FloatArray
        dataSize = data.size * Float.SIZE_BYTES
        buffer = ByteBuffer
            .allocateDirect(dataSize)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(data).position(0) }
        GLES30.glBindBuffer(GLES32.GL_TEXTURE_BUFFER, bufferID);
        GLES30.glBufferData(GLES32.GL_TEXTURE_BUFFER, dataSize, null, GLES32.GL_DYNAMIC_DRAW)
        GLES30.glBufferSubData(GLES32.GL_TEXTURE_BUFFER, 0, dataSize, buffer)

        id = generateTextureID()
        GLES30.glActiveTexture(channel)
        GLES30.glBindTexture(GLES32.GL_TEXTURE_BUFFER, id)
        GLES32.glTexBuffer(GLES32.GL_TEXTURE_BUFFER, GLES30.GL_R32F, bufferID)
        GLES30.glBindBuffer(GLES32.GL_TEXTURE_BUFFER, 0)
    }

    private fun setData(data: FloatArray) {
        assert(data.size * Float.SIZE_BYTES == dataSize)
        buffer.put(data).position(0)
        GLES30.glBindBuffer(GLES32.GL_TEXTURE_BUFFER, bufferID);
        GLES30.glBufferSubData(GLES32.GL_TEXTURE_BUFFER, 0, dataSize, buffer)
        GLES30.glBindBuffer(GLES32.GL_TEXTURE_BUFFER, 0)
    }

    private fun generateTextureID() : Int {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            throw java.lang.RuntimeException("It's not possible to generate ID for texture")
        }
        return textureIds[0]
    }

    private fun generateBufferID(): Int {
        val bufferIds = IntArray(1)
        GLES30.glGenBuffers(1, bufferIds, 0)
        if (bufferIds[0] == 0) {
            throw java.lang.RuntimeException("It's not possible to generate buffer ID for texture")
        }
        return bufferIds[0]
    }
}