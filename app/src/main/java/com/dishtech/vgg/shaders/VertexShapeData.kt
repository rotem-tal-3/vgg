package com.dishtech.vgg.shaders

import android.opengl.GLES30
import com.dishtech.vgg.ext.toByteBuffer

class VertexShapeData private constructor(private val vertices: FloatArray, val drawMode: Int) {
    val uvOffset: Int = when (drawMode) {
        GLES30.GL_TRIANGLE_STRIP, GLES30.GL_TRIANGLE_FAN, GLES30. GL_TRIANGLES -> 3
        GLES30.GL_LINE_STRIP, GLES30.GL_LINES, GLES30.GL_LINE_LOOP -> 2
        GLES30.GL_POINTS -> 1
        else -> throw RuntimeException("Unrecognized draw mode $drawMode")
    }

    override fun hashCode(): Int {
        return vertices.contentHashCode() * 32 + drawMode.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is VertexShapeData) {
            return false
        }
        return (vertices contentEquals other.vertices) && (drawMode == other.drawMode)
    }

    val size = vertices.size / VERTEX_SIZE

    val buffer = vertices.toByteBuffer()

    companion object {

        // The input data for each vertex is 3 position points (x, y, z) and 2 texture coordinates.
        private const val VERTEX_SIZE = 5

        fun cube3D(): VertexShapeData {
            val vertices = floatArrayOf(
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
            )
            return VertexShapeData(vertices, GLES30.GL_TRIANGLES)
        }

        fun quad(): VertexShapeData {
            val vertices = floatArrayOf(
                -1.0f, -1.0f, 0f, 0f, 1f,
                1.0f, -1.0f, 0f, 1f, 1f,
                -1.0f, 1.0f, 0f, 0f, 0f,
                1.0f, 1.0f, 0f, 1f, 0f
            )
            return VertexShapeData(vertices, GLES30.GL_TRIANGLE_STRIP)
        }
    }
}