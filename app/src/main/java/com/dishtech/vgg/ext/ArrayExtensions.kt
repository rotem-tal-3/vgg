package com.dishtech.vgg.ext

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun <T> Array<T>.mapInPlace(transform: (T) -> T) {
    for (i in this.indices) {
        this[i] = transform(this[i])
    }
}

fun FloatArray.mapInPlace(transform: (Float) -> Float) {
    for (i in this.indices) {
        this[i] = transform(this[i])
    }
}

fun IntArray.mapInPlace(transform: (Int) -> Int) {
    for (i in this.indices) {
        this[i] = transform(this[i])
    }
}

fun FloatArray.toByteBuffer(): FloatBuffer {
    val byteBuffer = ByteBuffer
        .allocateDirect(size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    byteBuffer.put(this).position(0)
    return byteBuffer
}