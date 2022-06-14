package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.audio.AudioPlayer
import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate

class DefaultViewModelConfiguration(val inputHandlers: Array<FrameInputHandler>,
                                    val displayableFeatures: Array<DisplayableFeature>,
                                    val gestureResponders: Array<GestureDelegate>,
                                    val shaderHandler: ShaderHandler,
                                    val audioPlayer: AudioPlayer?)

class DefaultViewModel(configuration: DefaultViewModelConfiguration) : ShaderViewModel {

    override val components: Array<DisplayableFeature.UIComponents>
        get() {
            return configuration.displayableFeatures.fold(arrayOf()) { arr, feature ->
                return@fold arr + feature.components
            }
        }
    override val name: String
        get() = configuration.shaderHandler.name

    var configuration = configuration
        get() = field
        set(value) {
            field.shaderHandler.onSurfaceRevoked()
            field = value
            value.shaderHandler.onSurfaceRegained()
            value.audioPlayer?.play()
        }

    override fun onSurfaceRevoked() {
        configuration.shaderHandler.onSurfaceRevoked()
    }

    override fun onSurfaceRegained() {
        configuration.shaderHandler.onSurfaceRegained()
    }

    override fun initializeShaderIfNeeded() {
        configuration.shaderHandler.initializeShaderIfNeeded()
    }

    override fun surfaceCreated() {
        configuration.audioPlayer?.play()
        configuration.shaderHandler.initializeShaderIfNeeded()
        configuration.shaderHandler.onSurfaceRegained()
    }

    override fun drawFrame(timeInSeconds: Float) {
        for (inputHandler in configuration.inputHandlers) {
            inputHandler.drawFrame(timeInSeconds)
        }
        configuration.shaderHandler.drawFrame(timeInSeconds)
    }

    override fun onGesture(gesture: Gesture) {
        for (gestureResponder in configuration.gestureResponders) {
            gestureResponder.onGesture(gesture)
        }
    }
}