package com.dishtech.vgg.shaders

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import kotlinx.coroutines.yield

class Uniform(val name: String, val type: String, var value: Any)

interface GLResource {
    val uniform: Uniform
    val location: Int
    var needsSet: Boolean
}

interface GLTextureResource : GLResource {
    val id: Int
    val channel: Int
    val textureType: Int
}

class GLFloatResource(override val uniform: Uniform, override val location: Int) : GLResource {
    override var needsSet = false
}

class GLTexture(override val uniform: Uniform, override val location: Int,
                override val id: Int, override val channel: Int) : GLTextureResource {
    var lastBitmap: Bitmap = uniform.value as Bitmap
    override val textureType = GLES30.GL_TEXTURE_2D
    override var needsSet = false
        get() { return field }
        set(value) {
            field = value
            if (!value) { return }
            setBitmap()
        }

    private fun setBitmap() {
        val bitmap = uniform.value as Bitmap
        GLES30.glActiveTexture(channel)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        lastBitmap = bitmap
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }
}

class CubemapData(front: Bitmap,back: Bitmap, top: Bitmap, bottom: Bitmap,
                  left: Bitmap, right: Bitmap) : Iterable<Bitmap> {
    private val orderedData = arrayOf(right, left, top, bottom, back, front)
    override fun iterator(): Iterator<Bitmap> {
        return orderedData.iterator()
    }
}

class GLCubemap(override val uniform: Uniform, override val location: Int, override val id: Int,
                override val channel: Int) : GLTextureResource {
    override var needsSet = false
    override val textureType = GLES30.GL_TEXTURE_CUBE_MAP
}