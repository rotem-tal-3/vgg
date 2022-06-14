package com.dishtech.vgg.quadrenderer

interface ValueFeeder<T> {

    /**
     * Returns the value generated for time point [timeInSeconds]
     */
    fun valueForTime(timeInSeconds: Float) : T

    /**
     * The duration in seconds that the input for this object can provide data for.
     */
    fun duration() : Float
}