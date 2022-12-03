package com.dishtech.vgg.shaders

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix

object VertexHandler {
    class VertexMat4(value: FloatArray, private val name: String) {
        constructor(name: String) : this(FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
        }, name)
        init {
            require(value.size == 16)
        }

        var handle = UNKNOWN_ATTRIBUTE

        var value = value
            set(value) {
                isDirty = true
                field = value
            }
        var isDirty = true
            private set

        fun setToGPU() {
            assert(handle != UNKNOWN_ATTRIBUTE) { "No handle found for $name" }
            GLES30.glUniformMatrix4fv(handle, 1, false, value, 0)
            isDirty = false
        }

        fun loadHandle(program: Int) {
            handle  = GLES20.glGetUniformLocation(program, name)
        }
    }
    private const val UNKNOWN_ATTRIBUTE = -1

    // vertex shader attributes
    const val VERTEX_MODEL = "model"
    const val VERTEX_VIEW = "view"
    const val VERTEX_PROJECTION = "projection"
    const val VERTEX_SHADER_IN_POSITION = "inPosition"
    const val VERTEX_SHADER_IN_TEXTURE_COORD = "inTexCoord"
    const val VERTEX_MATRIX_STM = "uSTMatrix"

    // Offset of the position data.
    private const val VERTICES_DATA_POS_OFFSET = 0

    // Size of the position vector (x, y, z).
    private const val POS_SIZE = 3

    // Size of the texture coordinates. (x, y)
    private const val TEX_SIZE = 2

    // Offset of the texture coordinates.
    private const val VERTICES_DATA_TEX_OFFSET = POS_SIZE

    // Stride size is composed of the position vector and texture coordinates.
    private const val VERTICES_DATA_STRIDE_BYTES = (POS_SIZE + TEX_SIZE) * Float.SIZE_BYTES

    val model = VertexMat4(VERTEX_MODEL)
    val view = VertexMat4(VERTEX_VIEW)
    val projection = VertexMat4(VERTEX_PROJECTION)
    private val matrixSTM = VertexMat4(VERTEX_MATRIX_STM)
    private var inPositionHandle = UNKNOWN_ATTRIBUTE
    private var inTextureHandle = UNKNOWN_ATTRIBUTE
    private var verticesDataNeedsSet = true
    val defaultCube = VertexShapeData.cube3D()
    var shapeData = defaultCube
        set(value) {
            verticesDataNeedsSet = value != field
            field = value
        }

    var vertexShaderSource = """#version 300 es
precision mediump float;

uniform mat4 $VERTEX_MODEL;
uniform mat4 $VERTEX_VIEW;
uniform mat4 $VERTEX_PROJECTION;

uniform mat4 $VERTEX_MATRIX_STM;

in vec3 inPosition;
in vec2 inTexCoord;

out vec2 texCoord;
void main() {
    gl_Position = $VERTEX_PROJECTION * $VERTEX_VIEW * $VERTEX_MODEL * vec4(inPosition.xyz, 1);
    texCoord = ($VERTEX_MATRIX_STM * vec4(inTexCoord.xy, 0, 0)).xy;
}
"""

    fun loadAttributeLocations(program: Int) {
        // bind vector shader attributes
        inPositionHandle = GLES20.glGetAttribLocation(program, VERTEX_SHADER_IN_POSITION)
        ProgramUtils.checkGlError("glGetAttribLocation $VERTEX_SHADER_IN_POSITION")
        if (inPositionHandle == UNKNOWN_ATTRIBUTE) throw RuntimeException("Could not get attrib location for $VERTEX_SHADER_IN_POSITION")
        inTextureHandle = GLES20.glGetAttribLocation(program, VERTEX_SHADER_IN_TEXTURE_COORD)
        ProgramUtils.checkGlError("glGetAttribLocation $VERTEX_SHADER_IN_TEXTURE_COORD")
        if (inTextureHandle == UNKNOWN_ATTRIBUTE) throw RuntimeException("Could not get attrib location for $VERTEX_SHADER_IN_TEXTURE_COORD")
        verticesDataNeedsSet = true
        model.loadHandle(program)
        ProgramUtils.checkGlError("glGetUniformLocation $VERTEX_MODEL")
        view.loadHandle(program)
        ProgramUtils.checkGlError("glGetUniformLocation $VERTEX_VIEW")
        projection.loadHandle(program)
        ProgramUtils.checkGlError("glGetUniformLocation $VERTEX_PROJECTION")
        matrixSTM.loadHandle(program)
        ProgramUtils.checkGlError("glGetUniformLocation $VERTEX_MATRIX_STM")
        if (matrixSTM.handle == UNKNOWN_ATTRIBUTE) {
            throw RuntimeException("Could not get uniform location for $VERTEX_MATRIX_STM"
            )
        }
    }

    fun setMatrixOffset(x: Float, y: Float) {
    }

    fun loadShaderAttributesToGPU(forceLoad: Boolean = false) {
        if (verticesDataNeedsSet || forceLoad) {
            setAttribute(
                inPositionHandle, VERTEX_SHADER_IN_POSITION, 3,
                VERTICES_DATA_POS_OFFSET
            )
            setAttribute(
                inTextureHandle, VERTEX_SHADER_IN_TEXTURE_COORD, 2,
                VERTICES_DATA_TEX_OFFSET
            )
            verticesDataNeedsSet = false
        }
        setMatIfNeeded(model, forceLoad)
        setMatIfNeeded(view, forceLoad)
        setMatIfNeeded(projection, forceLoad)
        setMatIfNeeded(matrixSTM, forceLoad)
    }

    private fun setMatIfNeeded(vertexMat4: VertexMat4, forceLoad: Boolean) {
        if (vertexMat4.isDirty || forceLoad) {
            vertexMat4.setToGPU()
        }
    }

    /**
     * set values for attributes of input vertices
     */
    private fun setAttribute(attrLocation: Int, attrName: String, size: Int, offset: Int) {
        if (attrLocation == UNKNOWN_ATTRIBUTE) {
            return
        }
        shapeData.buffer.position(offset)
        GLES30.glVertexAttribPointer(
            attrLocation,
            size,
            GLES30.GL_FLOAT,
            false,
            VERTICES_DATA_STRIDE_BYTES,
            shapeData.buffer
        )
        ProgramUtils.checkGlError("glVertexAttribPointer $attrName")
        GLES30.glEnableVertexAttribArray(attrLocation)
        ProgramUtils.checkGlError("glEnableVertexAttribArray $attrName")
    }
}