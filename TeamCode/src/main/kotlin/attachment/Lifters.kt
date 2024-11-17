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


    private var goalPos = 0.0;
    // Goal
    private var isAtGoal = true
    private var isAtBottom = true

    init {
        // Reset encoders
        resetEncoder()

        // Reverse right motor
        rightLifter.direction = DcMotorSimple.Direction.REVERSE
    }


    fun modifyGoal(difference: Double) {
        // moves the goal up or down
        goalPos += difference
        if (goalPos>1)
            goalPos = 1.0
        if (goalPos<0)
            goalPos = 0.0
    }
    fun actuate() {
        // if it's at the bottom, tries to move to the top, otherwise tries to move to the bottom
        // triggered by the y button
        goalPos =
            if (goalPos ==0.0)
                1.0
            else
                0.0
    }
    fun moveToGoalPos() {

    }

    fun resetEncoder() {
        lifterEncoder.resetEncoder()
    }

    fun lift(power: Double) {
        // Check if lifters are moving
        // if (!isAtGoal) return
        val currentPosition = lifterEncoder.currentPositions(Encoder.UnitType.ROTATIONS).sum()
        // Reset encoder if at bottom
        if (lifterEncoder.shouldTimeout())
            if (goalPos/5.6 <currentPosition)
                resetEncoder()
            else
                goalPos -= 0.1

        // Get current position
        lifterEncoder.startEncoderWithUnits(power, goalPos*5.6-currentPosition, Encoder.UnitType.ROTATIONS)
        /*
        if (currentPosition > 100) {
            // Retract
            lifterEncoder.startEncoderWithUnits(power, -5.6, Encoder.UnitType.ROTATIONS)
            isAtBottom = true
        } else {
            // Extend
            lifterEncoder.startEncoderWithUnits(power, 5.6, Encoder.UnitType.ROTATIONS)
            isAtBottom = false
        }
         */
        isAtGoal = false
    }

    fun moving() : Boolean {
        // Check if the motors are moving
        return rightLifter.isBusy && leftLifter.isBusy
    }
}