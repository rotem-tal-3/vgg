package com.dishtech.vgg.engine

import android.content.res.Resources
import android.opengl.Matrix
import com.dishtech.vgg.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Vec3f(val x: Float, val y: Float, val z: Float) {
    constructor(float: Float) : this(float, float, float)
}

class Camera(val position: Vec3f, val target: Vec3f, val up: Vec3f)
class World(val scale: Vec3f, val position: Vec3f, val rotation: Vec3f, val zNear: Float,
            val zFar: Float)

object Projections {
    val aspectRatio = Resources.getSystem().displayMetrics.widthPixels.toFloat() /
            Resources.getSystem().displayMetrics.heightPixels

//    fun mvpProjection() {
//        val cameraTransform = cameraTransform(camera)
//        val cameraPerspective = cameraTransform * perspective
//        val cameraTranslation = (-camera.position).toTranslationMatrix()
//        val worldTranslation = world.position.toTranslationMatrix()
//        val worldRotation = rotationTransform(world.rotation)
//        val worldScale = FloatArray(16)
//        Matrix.setIdentityM(worldScale, 0)
//        Matrix.scaleM(worldScale, 0, world.scale.x, world.scale.y, world.scale.z)
//        return worldScale * worldRotation * worldTranslation * cameraTranslation * cameraPerspective
//    }

    fun modelTransform(position: Vec3f, rotationAngle: Float, rotation: Vec3f): FloatArray {
        return FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
            Matrix.translateM(this, 0, position.x, position.y, position.z)
            Matrix.rotateM(this, 0, rotationAngle, rotation.x, rotation.y, rotation.z)
        }

    }

    /**
     * Returns a 4x4 perspective projection matrix from the given arguments. Where [alpha] is the
     * FOV of the camera in radians, [nearZ] and [farZ] define the size of the plane on the Z axis.
     */
    fun perspectiveProjection(alpha: Float, nearZ: Float, farZ: Float): FloatArray {
        val tanComponent = Math.tan(alpha / 2.0).toFloat()
        val zDist = farZ - nearZ
        return floatArrayOf(
            1f / (aspectRatio * tanComponent), 0f, 0f, 0f,
            0f, 1f / tanComponent, 0f, 0f,
            0f, 0f, -farZ / zDist, -1f,
            0f, 0f, -farZ * nearZ / zDist, 0f
        )
    }

    fun lookAt(camera: Camera): FloatArray {
        val transform = cameraTransform(camera)
        val lookAt = FloatArray(16)
        val translation = FloatArray(16)
        Matrix.setIdentityM(translation, 0)
        Matrix.translateM(translation, 0, -camera.position.x, -camera.position.y,
                          -camera.position.z)
        Matrix.multiplyMM(lookAt, 0, translation, 0, transform, 0)
        return lookAt
    }

    private fun cameraTransform(camera: Camera): FloatArray {
        val direction = (camera.position - camera.target).normalized()
        val right = camera.up.crossProduct(direction).normalized()
        val up = direction.crossProduct(right)
        return floatArrayOf(
            right.x, up.x, direction.x, 0f,
            right.y, up.y, direction.y, 0f,
            right.z, up.z, direction.z, 0f,
            0f, 0f, 0f, 1f
        )
    }
//
//    fun rotationTransform(vec: Vec3f) = rotationTransform(vec.x, vec.y, vec.z)
//
//    fun rotationTransform(x: Float, y: Float, z: Float): FloatArray {
//        return rotationZ(z) * rotationY(y) * rotationX(x)
//    }
//
//    fun rotationTransformZYX(vec: Vec3f) = rotationTransformZYX(vec.x, vec.y, vec.z)
//
//    fun rotationTransformZYX(x: Float, y: Float, z: Float): FloatArray {
//        return rotationX(x) * rotationY(y) * rotationZ(z)
//    }
//
//    fun rotationX(x: Float) = floatArrayOf(
//            1f, 0f, 0f, 0f,
//            0f, cos(x), -sin(x), 0f,
//            0f, sin(x), cos(x), 0f,
//            0f, 0f, 0f, 1f
//    )
//
//    fun rotationY(y: Float) = floatArrayOf(
//            cos(y), 0f, -sin(y), 0f,
//            0f, 1f, 0f, 0f,
//            sin(y), 0f, cos(y), 0f,
//            0f, 0f, 0f, 1f
//    )
//
//    fun rotationZ(z: Float) = floatArrayOf(
//            cos(z), -sin(z), 0f, 0f,
//            sin(z), cos(z), 0f, 0f,
//            0f, 0f, 1f, 0f,
//            0f, 0f, 0f, 1f
//    )
//
//    private fun Vec3f.to4Array(vec3f: Vec3f) : FloatArray {
//        return floatArrayOf(vec3f.x, vec3f.y, vec3f.z, 1f)
//    }
}