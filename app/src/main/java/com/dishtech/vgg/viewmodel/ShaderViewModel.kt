package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.quadrenderer.RenderDelegate
import com.dishtech.vgg.ui.gestures.GestureDelegate

interface DisplayableFeature {
    enum class UIComponents {
        SLIDER, PICTURE_BUTTON
    }
    val components : Array<UIComponents>
}

interface ShaderHandler : FrameInputHandler {

    val name: String

    /**
     * Called after the shader represented by this view was removed from being the used program.
     */
    fun onSurfaceRevoked()

    /**
     * Called after the shader represented by this view has gained access to the surface.
     */
    fun onSurfaceRegained()

    fun initializeShaderIfNeeded()
}

interface ShaderViewModel : ShaderHandler, RenderDelegate, GestureDelegate, DisplayableFeature {
    companion object {
        const val UNINITIALIZED = "Uninitialized"
    }
}