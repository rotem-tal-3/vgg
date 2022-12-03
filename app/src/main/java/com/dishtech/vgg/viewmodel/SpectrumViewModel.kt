package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.audio.AudioPlayer
import com.dishtech.vgg.audio.AudioPlayerSyncer
import com.dishtech.vgg.normalizeInPlace
import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.shaders.SpectrumShader
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate

class SpectrumViewModel(private val audioPlayerSyncer: AudioPlayerSyncer) : FrameInputHandler,
                                                                            GestureDelegate,
                                                                            AudioPlayer {

    override fun setShaderValueForTime(timeInSeconds: Float) {
        val value = audioPlayerSyncer.valueForTime(timeInSeconds)
        val current = ShaderManager.getCurrentShader() ?: return
        assert(current is SpectrumShader)
        value.normalizeInPlace()
        (current as SpectrumShader).spectrum = value
    }

    override fun onGesture(gesture: Gesture) {
        // Nothing to do yet.
    }

    override fun play() {
        audioPlayerSyncer.play()
    }

    override fun pause() {
        audioPlayerSyncer.pause()
    }

    override fun reset() {
        audioPlayerSyncer.reset()
    }

    override val isPlaying: Boolean
        get() { return audioPlayerSyncer.isPlaying }
    override var isLooping: Boolean
        get()  { return audioPlayerSyncer.isLooping }
        set(value) { audioPlayerSyncer.isLooping = value }

}