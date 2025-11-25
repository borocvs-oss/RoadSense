package com.roadsense.edge.ai

import com.roadsense.edge.data.Event
import kotlin.math.abs
import kotlin.math.sqrt

class EventDetector {

    private val ACCEL_THRESHOLD = 3.0f       // m/s² za naglo kočenje
    private val GYRO_THRESHOLD = 150.0f     // deg/s za oštru promenu smera
    private val SPEED_LIMIT_PERCENT = 30.0   // prekoračenje brzine u %

    fun detectAccelerationEvent(accelX: Float, accelY: Float, accelZ: Float): Event? {
        val totalAccel = sqrt((accelX*accelX + accelY*accelY + accelZ*accelZ).toDouble())
        return if (totalAccel > ACCEL_THRESHOLD) {
            Event(type = Event.Type.HARD_BRAKE, value = totalAccel.toFloat())
        } else null
    }

    fun detectGyroEvent(gyroZ: Float): Event? {
        return if (abs(gyroZ) > GYRO_THRESHOLD) {
            Event(type = Event.Type.SHARP_TURN, value = gyroZ)
        } else null
    }

    fun detectSpeedEvent(currentSpeed: Float, expectedSpeed: Float): Event? {
        val diff = currentSpeed - expectedSpeed
        val percent = diff / expectedSpeed * 100
        return if (percent > SPEED_LIMIT_PERCENT) {
            Event(type = Event.Type.SPEEDING, value = diff)
        } else null
    }
}
