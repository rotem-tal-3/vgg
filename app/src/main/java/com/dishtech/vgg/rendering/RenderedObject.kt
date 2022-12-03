package com.dishtech.vgg.rendering

import com.dishtech.vgg.shaders.VertexShapeData


interface ObjectHandler {
    fun addObject(renderedObject: RenderedObject)
    fun removeObject(renderedObject: RenderedObject) : Boolean
}

class RenderedObject(var modelTransform: FloatArray, var shapeData: VertexShapeData,
                     var shaderName: String, val UUID: String) {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is RenderedObject) {
            return false
        }
        return modelTransform contentEquals other.modelTransform && shapeData == other.shapeData &&
                UUID == other.UUID && shaderName == other.shaderName
    }

    override fun hashCode(): Int {
        return modelTransform.contentHashCode() shl 24 + shapeData.hashCode() shl 16 +
                UUID.hashCode() shl 8 + shaderName.hashCode()
    }
}