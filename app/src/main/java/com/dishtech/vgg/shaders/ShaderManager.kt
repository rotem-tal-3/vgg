package com.dishtech.vgg.shaders

import android.content.res.Resources
import android.graphics.Bitmap
import com.dishtech.vgg.BitmapUtils
import com.dishtech.vgg.Color

private class StoredShader(val shader: Shader, val glProgram: CompiledProgram)

object ShaderManager {
    val aspectRatio = Resources.getSystem().getDisplayMetrics().widthPixels.toFloat() /
            Resources.getSystem().getDisplayMetrics().heightPixels
    private val DEFAULT_SPECTRUM = FloatArray(7)
    private val DEFAULT_TIME = 0f
    private val DEFAULT_SCHEME = BitmapUtils.bitmapForColors(Color.SCHEMES[0])
    private val programCompiler: ProgramCompiler = ProgramCompiler.singleton()
    private val shaderMap: MutableMap<String, StoredShader> = mutableMapOf()
    private val PARAMETERLESS = mapOf(
        "frag" to this::frag,
        "startunnel" to this::starTunnelShader,
        "fibo" to this::fibo,
        "bar" to this::barShader
    )

    private var current : StoredShader? = null

    fun getCurrentShader() = current?.shader

    fun renderShader(name: String) {
        if (shaderMap.containsKey(name)) {
            if (current?.shader?.name != name) {
                current = shaderMap[name]
                programCompiler.useProgram(shaderMap[name]!!.glProgram)
            } else {
                programCompiler.setProgram(shaderMap[name]!!.glProgram)
            }
        }
    }

    fun getShaderNamed(name: String) : Shader? {
        if (!shaderMap.containsKey(name) && PARAMETERLESS.containsKey(name)) {
            val shader = PARAMETERLESS[name]?.let { it() }
            if (shader != null) {
                addShader(shader)
            }
            return shader
        }
        return shaderMap[name]?.shader
    }

    fun setValueForShader(shader: String, uniformName: String, value: Any) {
        assert(shader == current?.shader?.name) {
            "Tried to set a value for $shader while ${current?.shader?.name} was attached."
        }
        val stored = shaderMap[shader] ?: return
        val resource = stored.glProgram.resources[uniformName] ?: return
        assert(value::class == resource.uniform.value::class)
        resource.uniform.value = value
        resource.needsSet = true
    }

    private fun addShader(shader: Shader) {
        shaderMap[shader.name] = StoredShader(shader, programCompiler.createProgram(shader))
    }

    fun tunnelShader(texture0: Bitmap, texture1: Bitmap, scheme: Bitmap) : tunnel {
        val tunnel = tunnel(DEFAULT_TIME, texture0, texture1, DEFAULT_SPECTRUM, scheme,
                            floatArrayOf(0.5f, 0.5f))
        addShader(tunnel)
        return tunnel
    }

    fun frag(): fragment {
        val fragment = fragment( DEFAULT_TIME)
        addShader(fragment)
        return fragment
    }

    fun starTunnelShader() : startunnel {
        val starTunnel = startunnel(DEFAULT_TIME)
        addShader(starTunnel)
        return starTunnel
    }

    fun barShader() : bar {
        val bar = bar(DEFAULT_SCHEME, DEFAULT_SPECTRUM)
        addShader(bar)
        return bar
    }

    fun fibo() : fibo {
        val fibo = fibo(DEFAULT_SCHEME, DEFAULT_SPECTRUM, DEFAULT_TIME, aspectRatio)
        addShader(fibo)
        return fibo
    }
}