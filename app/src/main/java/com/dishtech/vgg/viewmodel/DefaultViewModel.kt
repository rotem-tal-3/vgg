package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.audio.AudioPlayer
import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.rendering.ObjectHandler
import com.dishtech.vgg.rendering.RenderedObject
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate

class DefaultViewModelConfiguration(val inputHandlers: Array<FrameInputHandler>,
                                    val displayableFeatures: Array<DisplayableFeature>,
                                    val gestureResponders: Array<GestureDelegate>,
                                    val shaderHandler: ShaderHandler,
                                    val audioPlayer: AudioPlayer?)

class DefaultViewModel(val configuration: DefaultViewModelConfiguration) : ShaderViewModel {


    override val components: Array<DisplayableFeature.UIComponents>
        get() {
            return configuration.displayableFeatures.fold(arrayOf()) { arr, feature ->
                return@fold arr + feature.components
            }
        }
    override val name = configuration.shaderHandler.name

    override fun initializeShaderIfNeeded() {
        configuration.audioPlayer?.play()
        configuration.shaderHandler.initializeShaderIfNeeded()
    }

    override fun renderShader(renderedObjects: Collection<RenderedObject>) {
        configuration.shaderHandler.renderShader(renderedObjects)
    }

    override fun setShaderValueForTime(timeInSeconds: Float) {
        for (inputHandler in configuration.inputHandlers) {
            inputHandler.setShaderValueForTime(timeInSeconds)
        }
    }

    override fun onGesture(gesture: Gesture) {
        for (gestureResponder in configuration.gestureResponders) {
            gestureResponder.onGesture(gesture)
        }
    }

}