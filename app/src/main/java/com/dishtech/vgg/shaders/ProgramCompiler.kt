package com.dishtech.vgg.shaders

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import com.dishtech.vgg.BitmapUtils
import com.dishtech.vgg.rendering.RenderedObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

//interface CompiledShader{ val uniforms: Array<GLResource> }
//class CompiledFragmentShader(override val uniforms: Array<GLResource>) : CompiledShader
//class CompiledVertexShader(override val uniforms: Array<GLResource>, val inputs: Array<GLResource>)
//    : CompiledShader

class GLProgram(val programID: Int, val fragmentShaderID: Int, val uniforms: Array<Uniform>)

class CompiledProgram(val program: Int, val resources: Map<String, GLResource>)

class ProgramCompiler private constructor() {
    companion object {
        private const val BUFFER = "samplerBuffer"
        private const val CUBE = "samplerCube"
        private const val VEC = "vec"
        private const val MAT = "mat"
        private const val FLOAT = "float"
        private const val ARRAY = "[]"
        private const val GL_TEXTURE = "sampler2D"
        private val TEXTURE_CHANNEL = arrayOf(
            GLES30.GL_TEXTURE0, GLES30.GL_TEXTURE1,
            GLES30.GL_TEXTURE2, GLES30.GL_TEXTURE3,
            GLES30.GL_TEXTURE4, GLES30.GL_TEXTURE5,
            GLES30.GL_TEXTURE6, GLES30.GL_TEXTURE7,
            GLES30.GL_TEXTURE8, GLES30.GL_TEXTURE9,
            GLES30.GL_TEXTURE10, GLES30.GL_TEXTURE11,
            GLES30.GL_TEXTURE12, GLES30.GL_TEXTURE13,
            GLES30.GL_TEXTURE14, GLES30.GL_TEXTURE15,
            GLES30.GL_TEXTURE16, GLES30.GL_TEXTURE17,
            GLES30.GL_TEXTURE18, GLES30.GL_TEXTURE19,
            GLES30.GL_TEXTURE20, GLES30.GL_TEXTURE21
        )

        private val instance = ProgramCompiler()
        fun singleton(): ProgramCompiler {
            return instance
        }

        private const val UNKNOWN_ATTRIBUTE = -1
        private const val FAILED_UNIFORM_LOCATION = "Failed calling glGetUniformLocation on "

        private fun loadShader(shaderType: Int, source: String): Int {
            var shader = GLES30.glCreateShader(shaderType)
            if (shader != UNKNOWN_ATTRIBUTE) {
                GLES30.glShaderSource(shader, source)
                GLES30.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == UNKNOWN_ATTRIBUTE) {
                    GLES30.glDeleteShader(shader)
                    shader = UNKNOWN_ATTRIBUTE
                }
            }
            return shader
        }
    }

    var vertexShader = UNKNOWN_ATTRIBUTE

    fun createProgram(fragmentShader: Shader): CompiledProgram {
        if (vertexShader == UNKNOWN_ATTRIBUTE) {
            vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, VertexHandler.vertexShaderSource)
            if (vertexShader == UNKNOWN_ATTRIBUTE) {
                throw RuntimeException("Failed loading vertex shader")
            }
        }
        val fragmentID = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShader.rawString)
        if (fragmentID == UNKNOWN_ATTRIBUTE) {
            throw RuntimeException("Failed loading fragment shader ${fragmentShader.javaClass}")
        }
        val program = GLES30.glCreateProgram()
        if (program == UNKNOWN_ATTRIBUTE) {
            throw RuntimeException(
                "Failed creating program with fragment shader " +
                        "${fragmentShader.javaClass}"
            )
        }
        GLES30.glAttachShader(program, vertexShader)
        ProgramUtils.checkGlError("glAttachShader: vertex")
        GLES30.glAttachShader(program, fragmentID)
        ProgramUtils.checkGlError("glAttachShader: pixel")
        linkProgram(program)
        ProgramUtils.checkGlError("")
        val compiledUniforms = createUniformGLResources(fragmentShader.uniforms, program)

        return CompiledProgram(program, compiledUniforms)
    }

    fun useProgram(compiledProgram: CompiledProgram, renderedObjects: Collection<RenderedObject>) {
        VertexHandler.loadAttributeLocations(compiledProgram.program)
        GLES30.glUseProgram(compiledProgram.program)
        setProgram(compiledProgram, false, renderedObjects)
        GLES30.glFinish()
    }

    fun setProgram(compiledProgram: CompiledProgram, filter: Boolean = true,
                   renderedObjects: Collection<RenderedObject>) {
        setGLResources(if (filter) compiledProgram.resources.values.filter { it.needsSet }
                       else compiledProgram.resources.values)
        for (renderedObject in renderedObjects) {
            VertexHandler.model.value = renderedObject.modelTransform
            VertexHandler.shapeData = renderedObject.shapeData
            drawCurrentVertexArray(!filter)
        }
    }

    private fun drawCurrentVertexArray(forceLoad: Boolean = false) {
        VertexHandler.loadShaderAttributesToGPU(forceLoad)

        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES20.GL_BLEND)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        GLES30.glDrawArrays(VertexHandler.shapeData.drawMode, 0, VertexHandler.shapeData.size)
        ProgramUtils.checkGlError("glDrawArrays")
    }

    private fun linkProgram(program: Int): Int {
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e("TAG", "Could not link program: ")
            Log.e("TAG", GLES30.glGetProgramInfoLog(program))
            GLES30.glDeleteProgram(program)
            return UNKNOWN_ATTRIBUTE
        }
        return program
    }

    /**
     * creates GLResource objects from the uniforms stored at shader.uniforms
     */
    private fun createUniformGLResources(uniforms: Array<Uniform>,
                                         program: Int): Map<String, GLResource> {
        var textureCount = 0;
        return uniforms.map {
            val location = GLES30.glGetUniformLocation(program, it.name)
            val errorString = FAILED_UNIFORM_LOCATION + it.name
            ProgramUtils.checkGlError(errorString)
            if (location == UNKNOWN_ATTRIBUTE) throw RuntimeException(errorString)
            if (it.type == GL_TEXTURE) {
                val channel = TEXTURE_CHANNEL[textureCount]
                textureCount++
                val id = BitmapUtils.toGlTexture(it.value as Bitmap, false, channel)
                return@map Pair(it.name, GLTexture(it, location, id, channel))
            }
            if (it.type == CUBE) {
                val channel = TEXTURE_CHANNEL[textureCount]
                textureCount++
                val id = BitmapUtils.glCubeFromData(it.value as CubemapData, channel)
                return@map Pair(it.name, GLCubemap(it, location, id, channel))
            }
            if (it.type == BUFFER) {
                val channel = TEXTURE_CHANNEL[textureCount]
                textureCount++
                return@map Pair(it.name, TextureBuffer(it, location, channel))
            }
            return@map Pair(it.name, GLFloatResource(it, location))
        }.toMap()
    }

    private fun setGLResources(glResources: Collection<GLResource>) {
        val grouped = glResources.groupBy {
            if (it.uniform.type.startsWith(VEC)) {
                return@groupBy VEC
            } else if (it.uniform.type.startsWith(MAT)) {
                return@groupBy MAT
            }
            return@groupBy it.uniform.type
        }
        grouped[VEC]?.forEach(::setVectorResource)
        grouped[MAT]?.forEach(::setMatrixResource)
        grouped[FLOAT]?.forEach(::setFloatResource)
        grouped[GL_TEXTURE]?.forEach { setTextureResource(it as GLTexture) }
        grouped[FLOAT + ARRAY]?.forEach(::setFloatArrayResource)
    }

    private fun setFloatArrayResource(glResource: GLResource) {
        assert(glResource.uniform.value is FloatArray) {
            "Type mismatch, in loading GL resource ${glResource.uniform.name}. Expected " +
                    "FloatArray got: " + glResource.uniform.value.javaClass.toString()
        }
        val value = glResource.uniform.value as FloatArray
        val size = value.size
        val buffer = ByteBuffer.allocateDirect(size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(value).position(0)
            }
        GLES30.glUniform1fv(glResource.location, size, buffer)
        glResource.needsSet = false
    }

    private fun setVectorResource(glResource: GLResource) {
        assert(glResource.uniform.value is FloatArray) {
            "Type mismatch, in loading GL resource ${glResource.uniform.name}. Expected " +
                    "FloatArray got: " + glResource.uniform.value.javaClass.toString()
        }
        val value = glResource.uniform.value as FloatArray
        val size = glResource.uniform.type.last().digitToInt()
        assert(size == value.size) {
            "Size mismatch, expected: $size got: ${value.size}"
        }
        assert(size <= 4) {
            "unsupported size $size, Sizes up to 4 expected"
        }
        when (size) {
            1 -> GLES30.glUniform1fv(glResource.location, 1, value, 0)
            2 -> GLES30.glUniform2fv(glResource.location, 1, value, 0)
            3 -> GLES30.glUniform3fv(glResource.location, 1, value, 0)
            4 -> GLES30.glUniform4fv(glResource.location, 1, value, 0)
        }
        glResource.needsSet = false
    }

    private fun setMatrixResource(glResource: GLResource) {
        assert(glResource.uniform.value is FloatArray) {
            "Type mismatch, in loading GL resource ${glResource.uniform.name}. Expected " +
                    "FloatArray got: " + glResource.uniform.value.javaClass.toString()
        }
        val value = glResource.uniform.value as FloatArray
        val size = glResource.uniform.type.last().digitToInt()
        val sizeSquare = size * size
        assert(sizeSquare == value.size) {
            "Only squared matrices allowed, expected size: $sizeSquare got: ${value.size}"
        }
        assert(size in 2..4) {
            "unsupported size $size, Sizes are expected to be in range 2..4"
        }
        when (size) {
            2 -> GLES30.glUniformMatrix2fv(glResource.location, 1, false, value, 0)
            3 -> GLES30.glUniformMatrix3fv(glResource.location, 1, false, value, 0)
            4 -> GLES30.glUniformMatrix4fv(glResource.location, 1, false, value, 0)
        }
        glResource.needsSet = false
    }

    private fun setTextureResource(glResource: GLTexture) {
        assert(glResource.uniform.value is Bitmap) {
            "Type mismatch, in loading GL resource. Expected Bitmap got: " +
                    glResource.uniform.value.javaClass.toString()
        }
        GLES30.glUniform1i(glResource.location, 0)
        GLES30.glActiveTexture(glResource.channel)
        GLES30.glBindTexture(glResource.textureType, glResource.id)
        glResource.needsSet = false
    }

    private fun setFloatResource(glResource: GLResource) {
        assert(glResource.uniform.value is Float) {
            "Type mismatch, in loading GL resource ${glResource.uniform.name}. Expected " +
                    "Float got: " + glResource.uniform.value.javaClass.toString()
        }
        val value = glResource.uniform.value as Float
        GLES30.glUniform1f(glResource.location, value)
        glResource.needsSet = false
    }

//        private fun createInputGLResources(shader: Shader, program: Int): Array<GLResource> {
//            return shader.inputs.map {
//                val location = GLES20.glGetAttribLocation(program, it.name)
//                val errorString = FAILED_ATTRIBUTE_LOCATION + it.name
//                ProgramUtils.checkGlError(errorString)
//                if (location == UNKNOWN_ATTRIBUTE) throw RuntimeException(errorString)
//                return@map GLFloatResource(it, location)
//            }.toTypedArray()
//        }
//        private fun setCompiledVertexResources(compiledVertexShader: CompiledVertexShader) {
//            setInputs(compiledVertexShader.inputs)
//            setGLResources(compiledVertexShader.uniforms)
//        }
//        private fun setInputs(glResources: Array<GLResource>) {
//            var accumulatedOffset = 0;
//            glResources.forEach {
//                val size = it.uniform.type.last().digitToInt();
//                setAttribute(it.location, it.uniform.name, size, accumulatedOffset)
//                accumulatedOffset += size
//            }
//        }

}
