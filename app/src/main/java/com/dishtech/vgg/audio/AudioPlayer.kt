package com.dishtech.vgg.audio

interface AudioPlayer {
    fun play()
    fun pause()
    fun reset()
    val isPlaying: Boolean
    var isLooping: Boolean
}