package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.quadrenderer.RenderDelegate
import com.dishtech.vgg.rendering.ObjectHandler
import com.dishtech.vgg.rendering.RenderedObject
import com.dishtech.vgg.ui.gestures.GestureDelegate


interface DisplayableFeature {
    enum class UIComponents {
        SLIDER, PICTURE_BUTTON
    }
    val components : Array<UIComponents>
}

interface ShaderHandler {

    val name: String

    fun initializeShaderIfNeeded()

    fun renderShader(renderedObjects: Collection<RenderedObject>)
}

interface ShaderViewModel : FrameInputHandler, GestureDelegate, DisplayableFeature, ShaderHandler {
    companion object {
        const val UNINITIALIZED = "Uninitialized"
    }
}