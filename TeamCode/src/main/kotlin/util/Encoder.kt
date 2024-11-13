package util

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import kotlin.math.abs

class Encoder(private val motors: List<DcMotor>, private val countsPerMotorRev: Double, driveGearReduction: Double, wheelDiameterMM: Double) {
    constructor(motors: List<DcMotor>, countsPerMotorRev: Double) : this(motors, countsPerMotorRev, 1.0, 96.0)

    // Calculate counts per mm
    private val countsPerMM : Double = (countsPerMotorRev * driveGearReduction) / (wheelDiameterMM * Math.PI)

    // Target position
    var targetPositions = mutableListOf(0, 0)

    fun resetEncoder() {
        for (motor in motors) {
            // Reset encoder
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }

    private fun startEncoder(speed: Double, counts: Double) {
        for ((i, motor) in motors.withIndex()) {
            targetPositions[i] = motor.currentPosition + counts.toInt()
            motor.targetPosition = targetPositions[i]

            // Set mode and power
            motor.mode = DcMotor.RunMode.RUN_TO_POSITION
            motor.power = abs(speed)
        }
    }

    fun startEncoderWithMM(speed: Double, distance: Double) {
        // Calculate counts
        startEncoder(speed, distance * countsPerMM)
    }

    fun startEncoderWithRotations(speed: Double, rotations: Double) {
        // Calculate counts
        startEncoder(speed, rotations * countsPerMotorRev)
    }

    fun moving(motor: DcMotor) : Boolean {
        // Check if motor is busy
        return motor.isBusy
    }

    fun currentPositions() : MutableList<Int> {
        // Get current position
        val positionList : MutableList<Int> = mutableListOf()
        for (motor in motors) {
            positionList.add(motor.currentPosition)
        }
        return positionList
    }

    // fun targetPosition(motor: DcMotor) : Int {
    //    // Get target position
    //    return motor.targetPosition
    // }
}