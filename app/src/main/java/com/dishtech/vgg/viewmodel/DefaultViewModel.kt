package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.audio.AudioPlayer
import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate

class DefaultViewModelConfiguration(val inputHandlers: Array<FrameInputHandler>,
                                    val displayableFeatures: Array<DisplayableFeature>,
                                    val gestureResponders: Array<GestureDelegate>,
                                    val shaderViewModel: ShaderViewModel,
                                    val audioPlayer: AudioPlayer?)

class DefaultViewModel(configuration: DefaultViewModelConfiguration) : ShaderViewModel {

    override val components: Array<ShaderViewModel.UIComponents>
        get() {
            return configuration.displayableFeatures.fold(arrayOf()) { arr, feature ->
                return@fold arr + feature.components
            }
        }
    override val name: String
        get() = configuration.shaderViewModel.name

    var configuration = configuration
    get() = field
    set(value) {
        field.shaderViewModel.onSurfaceRevoked()
        field = value
        value.shaderViewModel.onSurfaceRegained()
        value.audioPlayer?.play()
    }

    override fun onSurfaceRevoked() {
        configuration.shaderViewModel.onSurfaceRevoked()
    }

    override fun onSurfaceRegained() {
    }

    override fun surfaceCreated() {
        configuration.audioPlayer?.play()
        configuration.shaderViewModel.surfaceCreated()
    }

    override fun drawFrame(timeInSeconds: Float) {
        for (inputHandler in configuration.inputHandlers) {
            inputHandler.drawFrame(timeInSeconds)
        }
        configuration.shaderViewModel.drawFrame(timeInSeconds)
    }

    override fun onGesture(gesture: Gesture) {
        for (gestureResponder in configuration.gestureResponders) {
            gestureResponder.onGesture(gesture)
        }
    }
}