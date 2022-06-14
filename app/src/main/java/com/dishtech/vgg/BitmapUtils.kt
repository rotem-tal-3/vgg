package com.dishtech.vgg

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import com.dishtech.vgg.shaders.ProgramUtils
import kotlin.math.max
import kotlin.math.pow

object BitmapUtils {
    private const val MAX_F = 255f
    private const val MAX = 255
    private const val A_POS = 24
    private const val R_POS = 16
    private const val G_POS = 8
    private const val FULL_ALPHA = 0xff shl A_POS
    private val HEX_MAP = mapOf(
        'A' to 10, 'B' to 11, 'C' to 12, 'D' to 13, 'E' to 14, 'F' to 15
    )

    fun R(hex: String): Int {
        return valueForHex(hex.substring(0, 2))
    }

    fun posR(value: Int) = value shl R_POS

    fun G(hex: String): Int {
        return valueForHex(hex.substring(2, 4))
    }

    fun posG(value: Int) = value shl G_POS

    fun B(hex: String): Int {
        return valueForHex(hex.substring(4, 6))
    }

    fun posB(value: Int) = value

    fun A(hex: String): Int {
        return if (hex.length == 8) valueForHex(hex.substring(6, 8)) else MAX
    }

    fun posA(value: Int) = value shl A_POS

    private fun valueForHex(hex: String): Int {
        var accumulated = 0
        var str = hex
        if (hex.startsWith("#")) {
            str = hex.substring(1)
        }
        if (hex.startsWith("0x")) {
            str = hex.substring(2)
        }
        val count = str.length
        for (c in str.withIndex()) {
            val pos = count - c.index - 1
            if (HEX_MAP[c.value] != null) {
                accumulated += hexValue(HEX_MAP[c.value]!!, pos)
            } else {
                accumulated += hexValue(c.value.digitToInt(), pos)
            }
        }
        return accumulated
    }

    private fun hexValue(d: Int, pos: Int) = d * 16.0.pow(pos).toInt()

    private fun blueColorForData(data: FloatArray): IntArray {
        val colorList = data.map { posB((it * MAX_F).toInt()) or FULL_ALPHA }
        return colorList.toIntArray()
    }

    private fun colorToInt(color: Color): Int {
        return posR(color.R) + posG(color.G) + posB(color.B) + posA(color.A)
    }

    private fun colorToIntScaled(color: Color, scale: Float): Int {
        return posR(scaleColorComponent(color.R, scale)) or
                posG(scaleColorComponent(color.G, scale)) or
                posB(scaleColorComponent(color.B, scale)) or
                posA(scaleColorComponent(color.A, scale))
    }

    private fun colorScaledBy(color: Color, f: Float): Color {
        return Color(
            scaleColorComponent(color.R, f), scaleColorComponent(color.G, f),
            scaleColorComponent(color.B, f), scaleColorComponent(color.A, f)
        )
    }

    private fun scaleColorComponent(
        component: Int,
        scale: Float
    ) = max((component * scale).toInt(), MAX)

    fun bitmapForColors(
        colors: Array<Color>,
        config: Bitmap.Config = Bitmap.Config.ARGB_8888
    ): Bitmap {
        val colorList = colors.map { colorToInt(it) }.toIntArray()
        return Bitmap.createBitmap(colorList, colorList.size, 1, config)
    }

    fun scaledColoredBitmap(
        scales: FloatArray,
        colors: Array<Color>,
        config: Bitmap.Config = Bitmap.Config.ARGB_8888,
        normalize: Boolean = true
    ): Bitmap {
        assert(colors.size == scales.size) {
            "Color and scales size must be equal. got ${colors.size} colors and ${
                scales
                    .size
            } scales"
        }
        if (normalize) {
            scales.normalizeInPlace()
        }
        val colorList = scales.zip(colors).map {
            colorToIntScaled(it.second, it.first)
        }.toIntArray()
        return Bitmap.createBitmap(colorList, colorList.size, 1, config)
    }

    /**
     * Returns a bitmap object with [data] populating the red channel and alpha channel set to
     * 0xFF. The returned bitmap height is 1 and width would be equal to [data].size
     */
    fun blueBitmap(
        data: FloatArray,
        config: Bitmap.Config = Bitmap.Config.ARGB_8888,
        normalize: Boolean = true
    ): Bitmap {
        if (normalize) {
            data.normalizeInPlace()
        }
        val color = blueColorForData(data)
        return Bitmap.createBitmap(color, color.size, 1, config)
    }

    /**
     * Load texture from Bitmap and write it to the video memory, returns the txture ID.
     * @needToRecycle - do we need to recycle current Bitmap when we write it GPI?
     */
    @Throws(RuntimeException::class)
    fun toGlTexture(bitmap: Bitmap, needToRecycle: Boolean, textureSlot: Int): Int {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            throw java.lang.RuntimeException("It's not possible to generate ID for texture")
        }

        GLES30.glActiveTexture(textureSlot)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])

        // texture filters
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )

        // write bitmap to GPU
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        if (needToRecycle) {
            bitmap.recycle()
        }

        // unbind texture from slot
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }

}

