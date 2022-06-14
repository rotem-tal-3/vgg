package com.dishtech.vgg.viewmodel

import android.graphics.Bitmap
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.shaders.tunnel
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate
import com.dishtech.vgg.ui.gestures.Gestures


class TunnelHandler(texture0: Bitmap, texture1: Bitmap,
                    val initialScheme: Bitmap) : ShaderHandler, GestureDelegate,
                                                 DisplayableFeature {
    private lateinit var shader : tunnel

    var texture0 = texture0
        set(value) {
            field = value
            if (this::shader.isInitialized) {
                shader.iChannel0 = value
            }
        }

    var texture1 = texture1
        set(value) {
            field = value
            if (this::shader.isInitialized) {
                shader.iChannel1 = value
            }
        }

    override val components = arrayOf(
        DisplayableFeature.UIComponents.PICTURE_BUTTON,
        DisplayableFeature.UIComponents.PICTURE_BUTTON
    )

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
            shader = ShaderManager.tunnelShader(texture0, texture1, initialScheme)
        }
    }

    override fun drawFrame(timeInSeconds: Float) {
        ShaderManager.renderShader(shader.name)
    }

    override fun onGesture(gesture: Gesture) {
        if (gesture.gesture == Gestures.TOUCH) {
            shader.touchLoc = gesture.params
        }
    }
}