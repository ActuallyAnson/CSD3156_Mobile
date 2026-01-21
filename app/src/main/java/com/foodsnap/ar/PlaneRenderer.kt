package com.foodsnap.ar

import android.util.Log
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders and manages AR plane visualization.
 *
 * Handles:
 * - Plane tracking state monitoring
 * - Plane filtering (horizontal only for food placement)
 * - Plane selection for model placement
 */
@Singleton
class PlaneRenderer @Inject constructor() {

    /**
     * Data class representing a detected plane suitable for food placement.
     */
    data class DetectedPlane(
        val plane: Plane,
        val centerX: Float,
        val centerY: Float,
        val centerZ: Float,
        val extentX: Float,
        val extentZ: Float
    )

    /**
     * Gets all horizontal planes suitable for food placement.
     *
     * @param planes Collection of tracked planes
     * @return List of horizontal planes that are tracking
     */
    fun getHorizontalPlanes(planes: Collection<Plane>): List<DetectedPlane> {
        return planes
            .filter { plane ->
                // Only horizontal upward-facing planes
                plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                plane.trackingState == TrackingState.TRACKING
            }
            .map { plane ->
                val center = plane.centerPose
                DetectedPlane(
                    plane = plane,
                    centerX = center.tx(),
                    centerY = center.ty(),
                    centerZ = center.tz(),
                    extentX = plane.extentX,
                    extentZ = plane.extentZ
                )
            }
            .also { planes ->
                Log.d(TAG, "Found ${planes.size} horizontal planes")
            }
    }

    /**
     * Finds the best plane for food placement based on size and position.
     *
     * Prefers larger planes that are at a reasonable height.
     *
     * @param planes Collection of tracked planes
     * @param preferredHeight Preferred height in meters (default: table height ~0.7m)
     * @return Best plane for placement or null if none suitable
     */
    fun findBestPlaneForPlacement(
        planes: Collection<Plane>,
        preferredHeight: Float = 0.7f
    ): DetectedPlane? {
        val horizontalPlanes = getHorizontalPlanes(planes)

        if (horizontalPlanes.isEmpty()) {
            return null
        }

        // Score each plane based on size and height proximity
        return horizontalPlanes.maxByOrNull { plane ->
            val area = plane.extentX * plane.extentZ
            val heightDiff = kotlin.math.abs(plane.centerY - preferredHeight)

            // Score: larger area is better, closer to preferred height is better
            val areaScore = area * 10f  // Weight area more
            val heightScore = 1f / (heightDiff + 0.1f)  // Inverse of height difference

            areaScore + heightScore
        }
    }

    /**
     * Checks if a plane is large enough for placing a food model.
     *
     * @param plane The detected plane
     * @param minSize Minimum size in meters (default: 0.15m or ~6 inches)
     * @return true if plane is large enough
     */
    fun isPlaneLargeEnough(plane: DetectedPlane, minSize: Float = 0.15f): Boolean {
        return plane.extentX >= minSize && plane.extentZ >= minSize
    }

    /**
     * Gets plane information as a human-readable string.
     *
     * @param plane The detected plane
     * @return Description of the plane
     */
    fun getPlaneDescription(plane: DetectedPlane): String {
        val widthCm = (plane.extentX * 100).toInt()
        val depthCm = (plane.extentZ * 100).toInt()
        val heightCm = (plane.centerY * 100).toInt()

        return "Surface: ${widthCm}cm x ${depthCm}cm at ${heightCm}cm height"
    }

    companion object {
        private const val TAG = "PlaneRenderer"
    }
}
