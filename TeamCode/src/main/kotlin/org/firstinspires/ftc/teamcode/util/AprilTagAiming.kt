package org.firstinspires.ftc.teamcode.util

import kotlin.math.abs
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection

class AprilTagAiming {
    companion object {
        private const val P = 0.022
        private const val D = 0.0001
        private const val GOAL_X = 0.0
        private const val ANGLE_TOLERANCE = 0.4
        private const val MAX_ANGULAR_VELOCITY = 1.0
        private const val MIN_DT_MS = 1L
    }

    private var lastError = 0.0
    private var lastTime = 0L
    private var isActive = false

    /**
     * Points the robot towards [tag].
     *
     * @return an angular velocity that points the robot towards [tag]
     */
    context(telemetry: Telemetry)
    fun pointTowardsAprilTag(tag: AprilTagDetection): Double {
        val now = System.currentTimeMillis()
        val error = GOAL_X - tag.ftcPose.bearing

        telemetry.addData("Aiming Error", "%.2f°".format(error))

        if (abs(error) < ANGLE_TOLERANCE) {
            reset()
            return 0.0
        }

        if (!isActive) {
            lastError = error
            lastTime = now
            isActive = true
            return -(P * error).coerceIn(-MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY)
        }

        val dt = (now - lastTime).coerceAtLeast(MIN_DT_MS)
        val derivative = (error - lastError) / dt
        val turnPower = P * error + D * derivative

        lastError = error
        lastTime = now

        return -turnPower.coerceIn(-MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY)
    }

    /**
     * Resets the PD controller state. Called automatically when the robot is within the angle
     * tolerance of the target, but should also be called manually to reset the controller state.
     */
    fun reset() {
        lastError = 0.0
        lastTime = 0L
        isActive = false
    }
}
