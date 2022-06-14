package com.dishtech.vgg.audio

import com.dishtech.vgg.quadrenderer.ValueFeeder
import com.dishtech.vgg.ext.mapInPlace

class AudioVectorFeeder(private val spectrumProvider: ValueFeeder<Spectrum>) :
    ValueFeeder<FloatArray> {
    override fun valueForTime(timeInSeconds: Float): FloatArray {
        val time = if (timeInSeconds < spectrumProvider.duration()) timeInSeconds else
            timeInSeconds % duration()
        val spectrum = spectrumProvider.valueForTime(time)
        val vec = AudioVectorProvider.compressedSoundVector(spectrum)
        val maxVec = vec.maxOf { it }
        vec.mapInPlace { it / maxVec }
        return vec
    }

    override fun duration() = spectrumProvider.duration()
}