package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.shaders.Shader
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.ui.gestures.Gesture

class ParameterlessShaderViewModel(private val shaderName: String) : ShaderViewModel {
    private lateinit var shader : Shader
    override val components = arrayOf<ShaderViewModel.UIComponents>()

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

    override fun surfaceCreated() {
        initializeShaderIfNeeded()
    }

    private fun initializeShaderIfNeeded() {
        if (!this::shader.isInitialized) {
            val attemptToCreate = ShaderManager.getShaderNamed(shaderName) ?:  throw
            RuntimeException("Failed loading shader $shaderName")
            shader = attemptToCreate

        }
    }

    override fun drawFrame(timeInSeconds: Float) {
        ShaderManager.renderShader(shader.name)
    }

    override fun onGesture(gesture: Gesture) {
    }
}