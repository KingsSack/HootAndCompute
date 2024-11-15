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
        if (moving())
            return

        // Get current position
        var currentPosition = 0
        for (position in currentPositions())
            currentPosition += position

        if (currentPosition > 100) {
            // Retract
            lifterEncoder.startEncoderWithUnits(power, -5.6, Encoder.UnitType.ROTATIONS)
        } else {
            // Reset because the lifter is at the bottom
            lifterEncoder.resetEncoder()
            // Extend
            lifterEncoder.startEncoderWithUnits(power, 5.6, Encoder.UnitType.ROTATIONS)
        }
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

    fun stopEncoder() {
        // Stop the encoder
        lifterEncoder.stopEncoder()
    }

    fun checkEncoderTimeout() {
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