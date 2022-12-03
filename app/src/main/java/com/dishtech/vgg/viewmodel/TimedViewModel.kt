package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.quadrenderer.FrameInputHandler
import com.dishtech.vgg.quadrenderer.RenderDelegate
import com.dishtech.vgg.shaders.SchemeShader
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.shaders.TimedShader
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate
import com.dishtech.vgg.ui.gestures.Gestures

class TimedViewModel(var timeTransform: (Float, params: FloatArray?) -> Float,
                     var params: FloatArray?,
                     var respondToGestures: Array<Gestures>) : FrameInputHandler, GestureDelegate {

    override fun setShaderValueForTime(timeInSeconds: Float) {
        val shader = ShaderManager.getCurrentShader() ?: return
        assert(shader is TimedShader)
        (shader as TimedShader).iTime = timeTransform(timeInSeconds, params)
    }

    override fun onGesture(gesture: Gesture) {
        if (gesture.gesture in respondToGestures) {
            params = gesture.params
        }
    }

}