package com.dishtech.vgg.viewmodel

import android.util.Log
import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.quadrenderer.RenderDelegate
import com.dishtech.vgg.rendering.ObjectHandler
import com.dishtech.vgg.rendering.RenderedObject
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate

class MainViewModel(val shaderViewModels: Array<ShaderViewModel>) : ObjectHandler, RenderDelegate,
                                                                FrameInputHandler, GestureDelegate,
                                                                DisplayableFeature {
    private val viewModels: Map<ShaderViewModel, MutableList<RenderedObject>>
    private var activeViewModel: ShaderViewModel

    init {
        viewModels = shaderViewModels.associateWith { mutableListOf() }
        activeViewModel = shaderViewModels[0]
    }

    fun activeShaderName() = activeViewModel.name

    override fun render() {
        for ((handler, objects) in viewModels) {
            if (objects.isNotEmpty()) {
                handler.renderShader(objects)
            }
        }
    }

    override fun surfaceCreated() {
        activeViewModel.initializeShaderIfNeeded()
    }

    override fun setShaderValueForTime(timeInSeconds: Float) {
        for ((handler, objects) in viewModels) {
            if (objects.isNotEmpty()) {
                handler.setShaderValueForTime(timeInSeconds)
            }
        }
    }

    override fun onGesture(gesture: Gesture) {
        activeViewModel.onGesture(gesture)
    }

    override val components: Array<DisplayableFeature.UIComponents>
        get() {
            return activeViewModel.components
        }

    override fun addObject(renderedObject: RenderedObject) {
        viewModels[activeViewModel]?.add(renderedObject)
    }

    override fun removeObject(renderedObject: RenderedObject): Boolean {
        val ans = viewModels[activeViewModel]?.remove(renderedObject)
        if (ans != null) {
            return ans
        }
        return false
    }

    fun setActiveShader(name: String) {
        val viewModel = viewModels.keys.firstOrNull{ it.name == name }
        if (viewModel == null) {
            Log.e("MainViewModel", "Requested unavailable shader $name")
            return
        }
        viewModel.initializeShaderIfNeeded()
        activeViewModel = viewModel
    }
}