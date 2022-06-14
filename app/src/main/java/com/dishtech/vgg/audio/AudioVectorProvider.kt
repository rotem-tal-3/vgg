package com.dishtech.vgg.audio

import com.dishtech.vgg.ext.mapInPlace

class AudioVectorProvider() {
    companion object {
        // Approximation of the frequency range division.
        private const val MIN_HEARABLE = 16f
        private const val SUB_BASS_LIMIT = 60f
        private const val BASS_LIMIT = 250f
        private const val LOW_MID_LIMIT = 500f
        private const val MID_LIMIT = 2000f
        private const val HIGH_MID_LIMIT = 4000f
        private const val PRESENCE_LIMIT = 6000f
        private const val BRILLIANCE_LIMIT = 20000f
        private val RANGES = floatArrayOf(MIN_HEARABLE, SUB_BASS_LIMIT, BASS_LIMIT,
                                          LOW_MID_LIMIT, MID_LIMIT, HIGH_MID_LIMIT,
                                          PRESENCE_LIMIT, BRILLIANCE_LIMIT)

        fun compressedSoundVector(spectrum: Spectrum): FloatArray {
            val spectrumSize = spectrum.spectrum.size
            val topFreq = minOf( spectrum.bandWidth * spectrumSize, BRILLIANCE_LIMIT + 1f)
            val tempList = MutableList(0) { 0f }
            for (i in 1 until RANGES.size) {
                if (RANGES[i] > topFreq) {
                    break
                }
                tempList.add(tempList.size, averageForBands(spectrum, RANGES[i-1], RANGES[i]))
            }
            return tempList.toFloatArray()
        }

        private fun averageForBands(spectrum: Spectrum, lowerBand: Float, limitBand: Float): Float {
            var sum = 0f
            var count = 0
            for (i in 0 until spectrum.spectrum.size) {
                val band = spectrum.bandWidth * (i+1)
                if (band >= lowerBand) {
                    if (band <= limitBand) {
                        sum += spectrum.spectrum[i]
                        count++
                    } else {
                        if (count == 0) {
                            sum = spectrum.spectrum[i]
                            count++
                            break
                        }
                        break
                    }
                }
            }
            if (count == 0) {
                return spectrum.spectrum.last()
            }
            return sum / count
        }
    }
}