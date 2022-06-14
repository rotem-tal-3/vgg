package com.dishtech.vgg.viewmodel

import com.dishtech.vgg.BitmapUtils
import com.dishtech.vgg.Color
import com.dishtech.vgg.shaders.SchemeShader
import com.dishtech.vgg.shaders.ShaderManager
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate
import com.dishtech.vgg.ui.gestures.Gestures

class SchemeViewModel : GestureDelegate {
    private val SCHEMES = Color.SCHEMES.map(BitmapUtils::bitmapForColors).toTypedArray()

    private var schemeIndex = 0
    val initialScheme = SCHEMES[schemeIndex]

    private fun nextColorScheme() {
        schemeIndex = (schemeIndex + 1) % SCHEMES.size
        val shader = ShaderManager.getCurrentShader() ?: return
        assert(shader is SchemeShader)
        (shader as SchemeShader).scheme = SCHEMES[schemeIndex]
    }

    override fun onGesture(gesture: Gesture) {
        if (gesture.gesture == Gestures.SWIPE_UP) {
            nextColorScheme()
        }
    }

}