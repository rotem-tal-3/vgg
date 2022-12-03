package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.rendering.RenderedObject
import com.dishtech.vgg.shaders.Shader
import com.dishtech.vgg.shaders.ShaderManager

class ParameterlessShaderHandler(private val shaderName: String) : ShaderHandler {
    private lateinit var shader : Shader
    override val name: String get() {
        if (this::shader.isInitialized) {
            return shader.name
        }
        return ShaderViewModel.UNINITIALIZED
    }

    override fun initializeShaderIfNeeded() {
        if (!this::shader.isInitialized) {
            val attemptToCreate = ShaderManager.getShaderNamed(shaderName) ?:  throw
            RuntimeException("Failed loading shader $shaderName")
            shader = attemptToCreate

        }
    }

    override fun renderShader(renderedObjects: Collection<RenderedObject>) {
        initializeShaderIfNeeded()
        ShaderManager.renderShader(name, renderedObjects)
    }

}