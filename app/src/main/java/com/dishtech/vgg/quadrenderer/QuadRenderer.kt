package com.dishtech.vgg.quadrenderer

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.dishtech.vgg.shaders.VertexHandler
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

interface FrameInputHandler {
    fun setShaderValueForTime(timeInSeconds: Float)
}

interface RenderDelegate : FrameInputHandler {
    fun surfaceCreated()
    fun render()
}

class QuadRenderer(var delegate: WeakReference<RenderDelegate>) : GLSurfaceView.Renderer {
    companion object {
        private const val MILLI_TO_SEC = 0.001f;
    }

    private var initTime = System.currentTimeMillis()
    var timeSpeed = 1f

    fun resetAnimationTime() {
        initTime = System.currentTimeMillis()
    }

    fun setMatrixOffset(x: Float, y: Float) {
        VertexHandler.setMatrixOffset(x, y)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        delegate.get()?.surfaceCreated()
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        GLES30.glViewport(0, 0, p1, p2)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

        val timeInSeconds = (System.currentTimeMillis() - initTime).toFloat() * MILLI_TO_SEC
        delegate.get()?.setShaderValueForTime(timeInSeconds)
        delegate.get()?.render()
    }
}