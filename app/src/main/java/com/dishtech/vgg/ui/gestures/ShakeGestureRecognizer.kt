package com.dishtech.vgg.ui.gestures

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.lang.ref.WeakReference
import kotlin.math.pow

class ShakeGestureRecognizer(private val sensorManager: SensorManager,
                             private val delegate: WeakReference<GestureDelegate>) :
    SensorEventListener {
    companion object {
        private const val SHAKE_THRESHOLD = 350
        private const val UPDATE_FREQUENCY = 100L
        private const val SHAKE_TIMEOUT = 500
        private const val SHAKE_COUNT = 3
    }
    private var lastShakeTime = 0L
    private var shakeCount = 0

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) { return }
        val time = System.currentTimeMillis()
        val diff = time - lastShakeTime
        if (diff < UPDATE_FREQUENCY) { return }
        if (diff > SHAKE_TIMEOUT) {
            shakeCount = 0
        }
        lastShakeTime = time
        handleShakeEvent(isShake(event.values.copyOfRange(0, 3)))
    }

    private fun isShake(values: FloatArray) : Boolean {
        val acceleration =  values.reduce { accumulated, current ->
            accumulated + current.pow(2)
        } - SensorManager.GRAVITY_EARTH
        if (acceleration > SHAKE_THRESHOLD) {
            return true
        }
        return false
    }

    private fun handleShakeEvent(isShake: Boolean) {
        if (isShake) {
            shakeCount++
        }
        if (shakeCount == SHAKE_COUNT) {
            delegate.get()?.onGesture(Gesture(Gestures.SHAKE, floatArrayOf()))
            shakeCount = 0
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


}