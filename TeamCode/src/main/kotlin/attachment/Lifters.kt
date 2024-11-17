package attachment

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import util.Encoder

class Lifters(hardwareMap: HardwareMap, rightName: String, leftName: String) {
    // Initialize lifters
    private val rightLifter = hardwareMap.get(DcMotor::class.java, rightName)
    private val leftLifter = hardwareMap.get(DcMotor::class.java, leftName)

    // Encoder
    private val lifterEncoder = Encoder(listOf(rightLifter, leftLifter), 383.6, 2)

    // Goal
    private var isAtGoal = true
    private var isAtBottom = true

    init {
        // Reset encoders
        resetEncoder()

        // Reverse right motor
        rightLifter.direction = DcMotorSimple.Direction.REVERSE
    }

    fun resetEncoder() {
        lifterEncoder.resetEncoder()
    }

    fun lift(power: Double) {
        // Check if lifters are moving
        if (!isAtGoal) return

        // Reset encoder if at bottom
        if (isAtBottom)
            resetEncoder()

        // Get current position
        var currentPosition = 0
        for (position in currentPositions())
            currentPosition += position

        if (currentPosition > 100) {
            // Retract
            lifterEncoder.startEncoderWithUnits(power, -5.6, Encoder.UnitType.ROTATIONS)
            isAtBottom = true
        } else {
            // Extend
            lifterEncoder.startEncoderWithUnits(power, 5.6, Encoder.UnitType.ROTATIONS)
            isAtBottom = false
        }
        isAtGoal = false
    }

    fun setPower(power: Double) {
        // Set lifter power
        rightLifter.power = power
        leftLifter.power = power
    }

    fun currentPositions() : MutableList<Int> {
        // Get current position
        return lifterEncoder.currentPositions()
    }

    fun targetPositions() : MutableList<Int> {
        // Get target position
        return lifterEncoder.targetPositions
    }

    private fun stopEncoder() {
        // Stop the encoder
        lifterEncoder.stopEncoder()
    }

    fun checkForCompletion() {
        // Check if the lifters are at the goal
        if (lifterEncoder.checkEncoderTargets()) {
            isAtGoal = true
        }
        else {
            checkEncoderTimeout()
        }
    }

    private fun checkEncoderTimeout() {
        // Make sure the encoder is at the bottom
        if (!isAtBottom) return

        // Check if the encoder should timeout
        if (lifterEncoder.shouldTimeout()) {
            stopEncoder()
        }
    }

    fun moving() : Boolean {
        // Check if the motors are moving
        return rightLifter.isBusy && leftLifter.isBusy
    }
}