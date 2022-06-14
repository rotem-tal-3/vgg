package com.dishtech.vgg.shaders

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object VertexHandler {
    private const val UNKNOWN_ATTRIBUTE = -1

    // vertex shader attributes
    const val VERTEX_SHADER_IN_POSITION = "inPosition"
    const val VERTEX_SHADER_IN_TEXTURE_COORD = "inTexCoord"
    const val VERTEX_SHADER_UNIFORM_MATRIX_MVP = "uMVPMatrix"
    const val VERTEX_SHADER_UNIFORM_MATRIX_STM = "uSTMatrix"

    // position coordinates start from the starts from the start of array of each vertex
    private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0

    // 5 floats for each vertex (3 floats is a position and 2 texture coordinate)
    private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * Float.SIZE_BYTES

    // texture coordinates start from 3rd float (4th and 5th float)
    private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

    // 5 Rows (x,y,z, U,V) times 4 vertices needed to create a quad.
    private const val GENERIC_QUAD_VERTICES_SIZE = 20 * Float.SIZE_BYTES
    private val GENERIC_QUAD_VERTICES = ByteBuffer
        .allocateDirect(GENERIC_QUAD_VERTICES_SIZE)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(
                floatArrayOf(
                    // [x,y,z, U,V]
                    -1.0f, -1.0f, 0f, 0f, 1f,
                    1.0f, -1.0f, 0f, 1f, 1f,
                    -1.0f, 1.0f, 0f, 0f, 0f,
                    1.0f, 1.0f, 0f, 1f, 0f
                )
            ).position(0)
        }

    var matrixMVP = FloatArray(16)
    get() = field
    set(value) {
        field = value
        needsSet = true
    }

    private val matrixSTM = FloatArray(16)
    private var inPositionHandle = UNKNOWN_ATTRIBUTE
    private var inTextureHandle = UNKNOWN_ATTRIBUTE
    private var uMVPMatrixHandle = UNKNOWN_ATTRIBUTE
    private var uSTMatrixHandle = UNKNOWN_ATTRIBUTE
    private var needsSet = true
    var vertexShaderSource = """#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;
in vec3 inPosition;
in vec2 inTexCoord;
out vec2 texCoord;
void main() {
    gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);
    texCoord = (uSTMatrix * vec4(inTexCoord.xy, 0, 0)).xy;
}
"""

    init {
        Matrix.setIdentityM(matrixSTM, 0)
        Matrix.setIdentityM(matrixMVP, 0)
    }

    fun loadAttributeLocations(program: Int) {
        // bind vector shader attributes
        inPositionHandle = GLES20.glGetAttribLocation(program, VERTEX_SHADER_IN_POSITION)
        ProgramUtils.checkGlError("glGetAttribLocation $VERTEX_SHADER_IN_POSITION")
        if (inPositionHandle == UNKNOWN_ATTRIBUTE) throw RuntimeException("Could not get attrib location for $VERTEX_SHADER_IN_POSITION")
        inTextureHandle = GLES20.glGetAttribLocation(program, VERTEX_SHADER_IN_TEXTURE_COORD)
        ProgramUtils.checkGlError("glGetAttribLocation $VERTEX_SHADER_IN_TEXTURE_COORD")
        if (inTextureHandle == UNKNOWN_ATTRIBUTE) throw RuntimeException("Could not get attrib location for $VERTEX_SHADER_IN_TEXTURE_COORD")
        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, VERTEX_SHADER_UNIFORM_MATRIX_MVP)
        ProgramUtils.checkGlError("glGetUniformLocation $VERTEX_SHADER_UNIFORM_MATRIX_MVP")
        if (uMVPMatrixHandle == UNKNOWN_ATTRIBUTE) throw RuntimeException("Could not get uniform location for $VERTEX_SHADER_UNIFORM_MATRIX_MVP")
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, VERTEX_SHADER_UNIFORM_MATRIX_STM)
        ProgramUtils.checkGlError("glGetUniformLocation $VERTEX_SHADER_UNIFORM_MATRIX_STM")
        if (uSTMatrixHandle == UNKNOWN_ATTRIBUTE) throw RuntimeException("Could not get uniform location for $VERTEX_SHADER_UNIFORM_MATRIX_STM")
        needsSet = true
    }

    fun setMatrixOffset(x: Float, y: Float) {
    }

    fun loadShaderAttributesToGPU() {
        if (!needsSet) { return }
        setAttribute(
            inPositionHandle, VERTEX_SHADER_IN_POSITION, 3,
            TRIANGLE_VERTICES_DATA_POS_OFFSET
        )
        setAttribute(
            inTextureHandle, VERTEX_SHADER_IN_TEXTURE_COORD, 2,
            TRIANGLE_VERTICES_DATA_UV_OFFSET
        )
        GLES30.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, matrixMVP, 0)
        GLES30.glUniformMatrix4fv(uSTMatrixHandle, 1, false, matrixSTM, 0)
        needsSet = false
    }

    /**
     * set values for attributes of input vertices
     */
    private fun setAttribute(attrLocation: Int, attrName: String, size: Int, offset: Int) {
        if (attrLocation == UNKNOWN_ATTRIBUTE) {
            return
        }
        GENERIC_QUAD_VERTICES.position(offset)
        GLES30.glVertexAttribPointer(
            attrLocation,
            size,
            GLES30.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            GENERIC_QUAD_VERTICES
        )
        ProgramUtils.checkGlError("glVertexAttribPointer $attrName")
        GLES30.glEnableVertexAttribArray(attrLocation)
        ProgramUtils.checkGlError("glEnableVertexAttribArray $attrName")
    }
}