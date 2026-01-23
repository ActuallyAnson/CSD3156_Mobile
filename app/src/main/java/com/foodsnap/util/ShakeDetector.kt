package com.foodsnap.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects shake gestures using the device accelerometer.
 * Used to trigger random recipe discovery.
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var lastShakeTime: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastUpdate: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD = 800
        private const val SHAKE_COOLDOWN_MS = 1000L
        private const val UPDATE_INTERVAL_MS = 100
    }

    /**
     * Starts listening for shake events.
     */
    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    /**
     * Stops listening for shake events.
     */
    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            val currentTime = System.currentTimeMillis()

            if ((currentTime - lastUpdate) > UPDATE_INTERVAL_MS) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]

                val speed = sqrt(
                    ((x - lastX) * (x - lastX) +
                            (y - lastY) * (y - lastY) +
                            (z - lastZ) * (z - lastZ)).toDouble()
                ) / diffTime * 10000

                if (speed > SHAKE_THRESHOLD) {
                    if ((currentTime - lastShakeTime) > SHAKE_COOLDOWN_MS) {
                        lastShakeTime = currentTime
                        onShake()
                    }
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection
    }
}
