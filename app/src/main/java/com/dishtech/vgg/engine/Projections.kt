package com.dishtech.vgg.engine

import android.content.res.Resources
import android.opengl.Matrix
import com.dishtech.vgg.*
import kotlin.math.cos
import kotlin.math.sin

class Vec3f(val x: Float, val y: Float, val z: Float) {
    constructor(float: Float) : this(float, float, float)
}

class Camera(val position: Vec3f, val target: Vec3f, val up: Vec3f, val fov: Float)
class World(val scale: Vec3f, val position: Vec3f, val rotation: Vec3f, val zNear: Float,
            val zFar: Float)

object Projections {
    val aspectRatio = Resources.getSystem().getDisplayMetrics().widthPixels.toFloat() /
            Resources.getSystem().getDisplayMetrics().heightPixels

    fun mvpProjection(world: World, camera: Camera): FloatArray {
        val perspective = perspectiveProjection(camera.fov, world.zNear, world.zFar)
        val cameraTransform = cameraTransform(camera)
        val cameraPerspective = cameraTransform * perspective
        val cameraTranslation = (-camera.position).toTranslationMatrix()
        val worldTranslation = world.position.toTranslationMatrix()
        val worldRotation = rotationTransform(world.rotation)
        val worldScale = FloatArray(16)
        Matrix.setIdentityM(worldScale, 0)
        Matrix.scaleM(worldScale, 0, world.scale.x, world.scale.y, world.scale.z)
        return worldScale * worldRotation * worldTranslation * cameraTranslation * cameraPerspective
    }

    /**
     * Returns a 4x4 perspective projection matrix from the given arguments. Where [alpha] is the
     * FOV of the camera in radians, [nearZ] and [farZ] define the size of the plane on the Z axis.
     */
    fun perspectiveProjection(alpha: Float, nearZ: Float, farZ: Float): FloatArray {
        val tanComponent = Math.tan(alpha / 2.0).toFloat()
        val zDist = nearZ - farZ
        return floatArrayOf(
            1f / (aspectRatio * tanComponent), 0f, 0f, 0f,
            0f, 1f / tanComponent, 0f, 0f,
            0f, 0f, -(nearZ + farZ) / zDist, 2 * nearZ * farZ / zDist,
            0f, 0f, 1f, 0f
        )
    }

    fun cameraTransform(camera: Camera): FloatArray {
        val N = camera.target.normalized()
        val U = camera.up.crossProduct(camera.target).normalized()
        val V = N.crossProduct(U)
        return floatArrayOf(
            U.x, U.y, U.z, 0f,
            V.x, V.y, V.z, 0f,
            N.x, N.y, N.z, 0f,
            0f, 0f, 0f, 1f
        )
    }

    fun rotationTransform(vec: Vec3f) = rotationTransform(vec.x, vec.y, vec.z)

    fun rotationTransform(x: Float, y: Float, z: Float): FloatArray {
        return rotationZ(z) * rotationY(y) * rotationX(x)
    }

    fun rotationTransformZYX(vec: Vec3f) = rotationTransformZYX(vec.x, vec.y, vec.z)

    fun rotationTransformZYX(x: Float, y: Float, z: Float): FloatArray {
        return rotationX(x) * rotationY(y) * rotationZ(z)
    }

    fun rotationX(x: Float) = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, cos(x), -sin(x), 0f,
            0f, sin(x), cos(x), 0f,
            0f, 0f, 0f, 1f
    )

    fun rotationY(y: Float) = floatArrayOf(
            cos(y), 0f, -sin(y), 0f,
            0f, 1f, 0f, 0f,
            sin(y), 0f, cos(y), 0f,
            0f, 0f, 0f, 1f
    )

    fun rotationZ(z: Float) = floatArrayOf(
            cos(z), -sin(z), 0f, 0f,
            sin(z), cos(z), 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
    )

    private fun Vec3f.to4Array(vec3f: Vec3f) : FloatArray {
        return floatArrayOf(vec3f.x, vec3f.y, vec3f.z, 1f)
    }
}