package com.dishtech.vgg

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

fun getRawTextFile(resources: Resources, @RawRes resource: Int): String =
    resources.openRawResource(resource).bufferedReader().use { it.readText() }

fun loadBitmapForTexture(resources: Resources, @DrawableRes drawableRes: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inScaled = false // true by default. false if we need scalable image

    // load from resources
    return BitmapFactory.decodeResource(resources, drawableRes, options)
}
