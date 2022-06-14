package com.dishtech.vgg.shaders

import android.graphics.Bitmap

private class StoredShader(val shader: Shader, val glProgram: CompiledProgram)

object ShaderManager {
    private val programCompiler: ProgramCompiler = ProgramCompiler.singleton()
    private val shaderMap: MutableMap<String, StoredShader> = mutableMapOf()
    private val PARAMETERLESS = mapOf("frag" to this::frag,
                                      "startunnel" to this::starTunnelShader)

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
        val tunnel = tunnel(0f, texture0, texture1, FloatArray(7), scheme,
                            floatArrayOf(0.5f, 0.5f))
        addShader(tunnel)
        return tunnel
    }

    fun frag(): fragment {
        val fragment = fragment( 0f)
        addShader(fragment)
        return fragment
    }

    fun starTunnelShader() : startunnel {
        val starTunnel = startunnel(0f)
        addShader(starTunnel)
        return starTunnel
    }

    fun barShader(scheme: Bitmap) : bar {
        val bar = bar(scheme, FloatArray(7))
        addShader(bar)
        return bar
    }
}