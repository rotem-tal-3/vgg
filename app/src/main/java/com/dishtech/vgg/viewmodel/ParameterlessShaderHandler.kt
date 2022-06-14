package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.shaders.Shader
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.ui.gestures.Gesture

class ParameterlessShaderHandler(private val shaderName: String) : ShaderHandler {
    private lateinit var shader : Shader

    override val name: String get() {
        if (this::shader.isInitialized) {
            return shader.name
        }
        return ShaderViewModel.UNINITIALIZED
    }

    override fun onSurfaceRevoked() {
        // Nothing to do.
    }

    override fun onSurfaceRegained() {
        initializeShaderIfNeeded()
        ShaderManager.renderShader(shader.name)
    }

    override fun initializeShaderIfNeeded() {
        if (!this::shader.isInitialized) {
            val attemptToCreate = ShaderManager.getShaderNamed(shaderName) ?:  throw
            RuntimeException("Failed loading shader $shaderName")
            shader = attemptToCreate

        }
    }

    override fun drawFrame(timeInSeconds: Float) {
        ShaderManager.renderShader(shader.name)
    }

}