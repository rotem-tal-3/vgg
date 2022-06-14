package com.dishtech.vgg.audio

import android.graphics.Bitmap
import android.media.MediaPlayer
import com.dishtech.vgg.BitmapUtils
import com.dishtech.vgg.quadrenderer.ValueFeeder

class AudioPlayerSyncer(private val valueFeeder: ValueFeeder<Spectrum>,
                        private val mediaPlayer: MediaPlayer) : ValueFeeder<FloatArray>,
                                                                AudioPlayer {
    companion object {
        private const val MiLLI_TO_SEC = 0.001f
    }

    // Empty value used when the audio is not playing.
    val pauseValue = FloatArray(7)

    override fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun reset() = mediaPlayer.reset()

    override var isLooping: Boolean get() { return mediaPlayer.isLooping }
    set(value) { mediaPlayer.isLooping = value }

    override val isPlaying: Boolean get() { return mediaPlayer.isPlaying }

    override fun valueForTime(timeInSeconds: Float): FloatArray {
        if (!mediaPlayer.isPlaying) {
            return pauseValue
        }
        val spec = valueFeeder.valueForTime(mediaPlayer.currentPosition * MiLLI_TO_SEC)
        return AudioVectorProvider.compressedSoundVector(spec)
    }

    override fun duration() = valueFeeder.duration()
}