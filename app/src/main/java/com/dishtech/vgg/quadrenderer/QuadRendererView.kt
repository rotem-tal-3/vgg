package com.dishtech.vgg.quadrenderer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import java.lang.ref.WeakReference


class QuadRendererView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val quadRenderer: QuadRenderer
) : GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(OPENGL_VERSION)
        setRenderer(quadRenderer)
        // set render mode RENDERMODE_WHEN_DIRTY or RENDERMODE_CONTINUOUSLY
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setDelegate(delegate: WeakReference<RenderDelegate>) {
        quadRenderer.delegate = delegate
    }

    companion object {
        private const val OPENGL_VERSION = 3
    }
}