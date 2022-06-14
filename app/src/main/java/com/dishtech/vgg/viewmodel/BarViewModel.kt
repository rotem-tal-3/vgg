package com.dishtech.vgg.viewmodel

import android.graphics.Bitmap
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.shaders.bar
import com.dishtech.vgg.ui.gestures.Gesture

class BarViewModel(val initialScheme: Bitmap) : ShaderViewModel {
    private lateinit var shader : bar
    override val components = Array(0) { ShaderViewModel.UIComponents.PICTURE_BUTTON }

    override val name: String get() {
        if (this::shader.isInitialized) {
            return shader.name
        }
        return ShaderViewModel.UNINITIALIZED
    }

    override fun onSurfaceRevoked() {
    }


    override fun onSurfaceRegained() {
        initializeShaderIfNeeded()
        ShaderManager.renderShader(shader.name)
    }

    override fun surfaceCreated() = initializeShaderIfNeeded()

    private fun initializeShaderIfNeeded() {
        if (!this::shader.isInitialized) {
            shader = ShaderManager.barShader(initialScheme)
        }
    }

    override fun drawFrame(timeInSeconds: Float) {
        ShaderManager.renderShader(shader.name)
    }

    override fun onGesture(gesture: Gesture) {
        // Do nothing
    }
}