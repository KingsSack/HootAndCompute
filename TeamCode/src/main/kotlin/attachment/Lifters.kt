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
    private val lifterEncoder = Encoder(listOf(rightLifter, leftLifter), 383.6)

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

        var currentPosition = 0
        for (position in currentPositions())
            currentPosition += position

        if (currentPosition > 100) {
            // Retract
            lifterEncoder.startEncoderWithRotations(power, -5.0)
        } else {
            // Extend
            lifterEncoder.startEncoderWithRotations(power, 5.0)
        }
    }

    fun setPower(power: Double) {
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

    fun moving() : Boolean {
        return lifterEncoder.moving(rightLifter) && lifterEncoder.moving(leftLifter)
    }
}