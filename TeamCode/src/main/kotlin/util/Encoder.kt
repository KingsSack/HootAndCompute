package util

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.abs
import kotlin.math.roundToInt

class Encoder(private val motors: List<DcMotor>, private val countsPerMotorRev: Double, driveGearReduction: Double, wheelDiameterMM: Double, private val timeout: Int) {
    constructor(motors: List<DcMotor>, countsPerMotorRev: Double, timeout: Int) : this(motors, countsPerMotorRev, 1.0, 96.0, timeout)

    // Calculate counts per mm
    private val countsPerMM : Double = (countsPerMotorRev * driveGearReduction) / (wheelDiameterMM * Math.PI)

    // Target position
    var targetPositions = MutableList(motors.size) { 0 }

    // Runtime
    private val runtime : ElapsedTime = ElapsedTime()

    fun resetEncoder() {
        for (motor in motors) {
            // Reset encoder
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }

    private fun startEncoder(speed: Double, counts: Double, reversedMotors: List<Int>) {
        // Make sure motors are not busy
        stopEncoder()

        // Reset runtime
        runtime.reset()
        for ((i, motor) in motors.withIndex()) {
            // Set target position
            val adjustedCounts = if (reversedMotors.contains(i)) -counts.roundToInt() else counts.roundToInt()
            targetPositions[i] = adjustedCounts
            motor.targetPosition = adjustedCounts

            // Set motor mode and power
            motor.mode = DcMotor.RunMode.RUN_TO_POSITION
            motor.power = abs(speed)
        }
    }

    fun startEncoderWithUnits(speed: Double, value: Double, unitType: UnitType, reversedMotors: List<Int> = emptyList()) {
        // Calculate counts
        val counts = when(unitType) {
            UnitType.MM -> value * countsPerMM
            UnitType.ROTATIONS -> value * countsPerMotorRev
        }

        // Start encoder
        startEncoder(speed, counts, reversedMotors)
    }

    fun checkEncoderTargets() : Boolean {
        // Check if motors are busy
        for ((i, motor) in motors.withIndex()) {
            if (targetPositions[i] - 100 <= motor.currentPosition
                && motor.currentPosition <= targetPositions[i] + 100 )
                return false
        }
        return true
    }

    fun stopEncoder() {
        for (motor in motors) {
            // Stop encoder
            motor.power = 0.0
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }

    fun currentPositions() : MutableList<Int> {
        // Get current position
        val positionList : MutableList<Int> = mutableListOf()
        for (motor in motors) {
            positionList.add(motor.currentPosition)
        }
        return positionList
    }

    fun shouldTimeout() : Boolean {
        // Check if runtime is greater than timeout
        return runtime.seconds() > timeout
    }

    enum class UnitType {
        MM,
        ROTATIONS
    }
}