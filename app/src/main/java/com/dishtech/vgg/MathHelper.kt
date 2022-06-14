package com.dishtech.vgg

import android.opengl.Matrix
import com.dishtech.vgg.engine.Vec3f
import com.dishtech.vgg.ext.mapInPlace
import kotlin.math.sqrt

object MathHelper {

    fun nearestPowerOfTwo(x: Float, roundDown: Boolean = true): Int {
        var power = 2;
        if (power >= x) {
            return power
        }
        while (power <= x) {
            power = power shl 1
        }
        return if (roundDown) power shr 1 else power
    }

}

/**
 * Normalizes the values in this array to be in range 0..1
 * Expecting positive value array.
 */
fun FloatArray.normalizeInPlace() {
    val max = this.maxOf { it }
    if (max > 0f) {
        this.mapInPlace { it / max }
    }
}

fun Vec3f.norm(other: Vec3f = this) = sqrt(x * other.x + y * other.y + z * other.z)

fun Vec3f.normalized(): Vec3f {
    val norm = norm()
    if (norm == 0f) {
        return this
    }
    return Vec3f(x / norm, y / norm, z / norm)
}

operator fun Vec3f.unaryMinus() = Vec3f(-x, -y, -z)

operator fun Vec3f.plus(vec: Vec3f) = Vec3f(x + vec.x, y + vec.y, z + vec.z)

operator fun Vec3f.minus(vec: Vec3f) = Vec3f(x - vec.x, y - vec.y, z - vec.z)

operator fun Vec3f.div(float: Float) = Vec3f(x / float, y / float, z / float)

/**
 * Returns the result of the cross product of this vector and [other].
 * A cross product between two vectors produces a vector which is perpendicular to the plane
 * defined by the vectors.
 */
fun Vec3f.crossProduct(other: Vec3f): Vec3f {
    return Vec3f(y * other.z - z * other.y,
                 z * other.x - x * other.z,
                 x * other.y - y * other.x)
}

fun Vec3f.toTranslationMatrix(): FloatArray {
    val translation = FloatArray(16)
    Matrix.setIdentityM(translation, 0)
    Matrix.translateM(translation, 0, x, y, z)
    return translation
}

/**
 * Returns the result of matrix multiplication with [other]. Assuming this is a square matrix, and
 * [other] size is either the same as this or the square root of this. In the latter case the
 * inputs would be multiplied as a matrix-vector pair. The result size would be same as [other].
 *
 */
operator fun FloatArray.times(other: FloatArray) : FloatArray {
    val targetSize = other.size
    val result = FloatArray(targetSize)
    if (targetSize == size) {
        Matrix.multiplyMM(result, 0, this, 0, other, 0)
    } else {
        assert(targetSize * targetSize == size) {
            "Size mismatch on matrix multiplication, tried to multiply arrays of size $targetSize" +
                    "and $size"
        }
        Matrix.multiplyMV(result, 0, this, 0, other, 0)
    }
    return result
}

/**
 * Performs a row major multiplication of [left] and [right].
 *
 * Returns the matrix multiplication of [left] * [right] considering only the first [d] dimensions
 * of both. Both input matrices should be of size greater or equal d squared.
 * Returns a float array with size d squared.
 */
fun squareMatmul(left: FloatArray, right: FloatArray, d: Int): FloatArray {
    val size = d * d
    assert(size <= left.size && size <= right.size)
    val result = FloatArray(size)
    for (i in 0 until d) {
        for (j in 0 until d) {
            for (k in 0 until d) {
                val rowOffset = i * d
                result[rowOffset + j] = left[rowOffset + k] * right[(k * d) + j]
            }
        }
    }
    return result
}
