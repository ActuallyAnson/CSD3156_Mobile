package com.foodsnap.ar

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages ARCore session lifecycle and availability checks.
 *
 * Handles:
 * - ARCore availability checking
 * - AR session creation and configuration
 * - Plane detection setup
 * - Session pause/resume
 */
@Singleton
class ARSessionManager @Inject constructor() {

    private var session: Session? = null
    private var installRequested = false

    /**
     * Result of AR availability check.
     */
    sealed class ARAvailability {
        object Supported : ARAvailability()
        object NotInstalled : ARAvailability()
        object NeedsUpdate : ARAvailability()
        data class Unsupported(val reason: String) : ARAvailability()
    }

    /**
     * Checks if ARCore is available on this device.
     *
     * @param context Application context
     * @return ARAvailability result
     */
    fun checkARAvailability(context: Context): ARAvailability {
        return when (ArCoreApk.getInstance().checkAvailability(context)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> ARAvailability.Supported
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> ARAvailability.NotInstalled
            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->
                ARAvailability.Unsupported("Device not capable")
            ArCoreApk.Availability.UNKNOWN_CHECKING,
            ArCoreApk.Availability.UNKNOWN_ERROR,
            ArCoreApk.Availability.UNKNOWN_TIMED_OUT ->
                ARAvailability.Unsupported("Unknown error")
            else -> ARAvailability.Unsupported("Unknown status")
        }
    }

    /**
     * Requests ARCore installation if needed.
     *
     * @param activity The activity to use for the install dialog
     * @return true if installation was requested, false if already installed
     */
    fun requestInstall(activity: Activity): Boolean {
        return try {
            val installStatus = ArCoreApk.getInstance().requestInstall(activity, !installRequested)
            when (installStatus) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    installRequested = true
                    true
                }
                ArCoreApk.InstallStatus.INSTALLED -> false
                else -> true
            }
        } catch (e: Exception) {
            Log.e(TAG, "ARCore install request failed", e)
            true
        }
    }

    /**
     * Creates and configures an AR session.
     *
     * @param context Application context
     * @return Configured AR session or null if creation failed
     */
    fun createSession(context: Context): Session? {
        if (session != null) {
            return session
        }

        return try {
            session = Session(context).apply {
                configure(
                    Config(this).apply {
                        // Enable plane detection for horizontal surfaces
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL

                        // Enable depth API if available for better occlusion
                        depthMode = if (isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                            Config.DepthMode.AUTOMATIC
                        } else {
                            Config.DepthMode.DISABLED
                        }

                        // Enable instant placement for faster anchoring
                        instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP

                        // Light estimation for realistic rendering
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                        // Update mode for 30fps
                        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    }
                )
            }
            Log.d(TAG, "AR session created successfully")
            session
        } catch (e: UnavailableArcoreNotInstalledException) {
            Log.e(TAG, "ARCore not installed", e)
            null
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Log.e(TAG, "User declined ARCore installation", e)
            null
        } catch (e: UnavailableApkTooOldException) {
            Log.e(TAG, "ARCore APK too old", e)
            null
        } catch (e: UnavailableSdkTooOldException) {
            Log.e(TAG, "SDK too old for ARCore", e)
            null
        } catch (e: UnavailableDeviceNotCompatibleException) {
            Log.e(TAG, "Device not compatible with ARCore", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AR session", e)
            null
        }
    }

    /**
     * Gets the current AR session.
     *
     * @return Current session or null if not created
     */
    fun getSession(): Session? = session

    /**
     * Pauses the AR session.
     */
    fun pauseSession() {
        session?.pause()
        Log.d(TAG, "AR session paused")
    }

    /**
     * Resumes the AR session.
     */
    fun resumeSession() {
        try {
            session?.resume()
            Log.d(TAG, "AR session resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume AR session", e)
        }
    }

    /**
     * Closes and destroys the AR session.
     */
    fun closeSession() {
        session?.close()
        session = null
        Log.d(TAG, "AR session closed")
    }

    /**
     * Checks if session is currently tracking.
     *
     * @return true if camera is tracking
     */
    fun isTracking(): Boolean {
        return try {
            session?.update()?.camera?.trackingState ==
                com.google.ar.core.TrackingState.TRACKING
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "ARSessionManager"
    }
}
