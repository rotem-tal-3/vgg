package com.dishtech.vgg.ui.gestures

enum class Gestures {
    SWIPE_UP, SWIPE_LEFT, SWIPE_DOWN, SWIPE_RIGHT, SHAKE, TOUCH
}

class Gesture(val gesture: Gestures, val params: FloatArray)

interface GestureDelegate {
    fun onGesture(gesture: Gesture)
}