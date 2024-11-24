package autonomous

import org.firstinspires.ftc.robotcore.external.Telemetry
import robot.Steve
import util.Position

class Auto(
    private val robot: Steve,
    private val samplePositions: List<Position>,
    private val basketPosition: Position,
    private val observationZonePosition: Position
): Autonomous(robot) {
    // States
    private enum class State {
        GO_TO_SAMPLE,
        COLLECT_SAMPLE,
        GO_TO_BASKET,
        DEPOSIT_SAMPLE,
        GO_TO_OBSERVATION_ZONE,
        STOP
    }

    // Default state
    private var state = State.GO_TO_SAMPLE

    // Sample index
    private var currentSampleIndex = 0

    // Completion
    private var verticalCompleted = true
    private var horizontalCompleted = true
    private var extenderCompleted = true
    // private var clawCompleted = true
    private var liftersCompleted = true

    // Strikes
    private var strikes = 0

    override fun run(telemetry: Telemetry) {
        // Display telemetry
        telemetry.addData("Numnber of strikes", strikes)
        telemetry.addData("Current state", state.toString())
        telemetry.addData("Current sample", currentSampleIndex + 1)

        // Check for obstacles
        checkForObstacles(telemetry)

        // State machine
        runState(telemetry)

        // Update the state
        telemetry.update()
    }

    private fun runState(telemetry: Telemetry) {
        when (state) {
            State.GO_TO_SAMPLE -> {
                // Check if the list is empty
                if (samplePositions.isEmpty()) {
                    state = State.GO_TO_OBSERVATION_ZONE
                    return
                }
                // Move to the sample position
                val position = samplePositions[currentSampleIndex]
                if (!moveToPosition(telemetry, position)) return
                state = State.COLLECT_SAMPLE
            }
            State.COLLECT_SAMPLE -> {
                // Collect the sample
                if (!collectSample()) return
                state = State.GO_TO_BASKET
            }
            State.GO_TO_BASKET -> {
                // Move to the basket position
                if (!moveToPosition(telemetry, basketPosition)) return
                state = State.DEPOSIT_SAMPLE
            }
            State.DEPOSIT_SAMPLE -> {
                // Deposit the sample
                if (!depositSample()) return
                if (currentSampleIndex < samplePositions.lastIndex) {
                    currentSampleIndex++
                    state = State.GO_TO_SAMPLE
                } else {
                    state = State.GO_TO_OBSERVATION_ZONE
                }
            }
            State.GO_TO_OBSERVATION_ZONE -> {
                // Go to the observation zone
                if (!moveToPosition(telemetry, observationZonePosition)) return
                state = State.STOP
            }
            State.STOP -> {
                // Stop the robot
                robot.halt()
            }
        }
    }

    private fun checkForObstacles(telemetry: Telemetry) {
        // Check for obstacles less than 300 mm away
        if (robot.getDistanceToObstacle(telemetry) < 300) {
            strikes++
            if (strikes >= 5) {
                state = State.STOP
            }
        }
    }

    private fun collectSample() : Boolean {
        // Extend claw
        if (!controlExtender(true)) return false  // Check for completion
        // Close claw
        if (!robot.openCloseClaw()) return false  // Check for completion
        // Retract claw
        if (!controlExtender(false)) return false  // Check for completion
        return true
    }

    private fun depositSample() : Boolean {
        // Face the basket
        robot.spinWithEncoder(0.9, -135.0)
        // Lift lifters
        if (!liftLifters()) return false  // Check for completion
        // Extend claw
        if (!controlExtender(true)) return false  // Check for completion
        // Open claw
        if (!robot.openCloseClaw()) return false  // Check for completion
        // Retract claw
        if (!controlExtender(false)) return false  // Check for completion
        // Spin back
        robot.spinWithEncoder(0.9, 135.0)
        return !robot.driving()  // Check for completion
    }

    private fun moveToPosition(telemetry: Telemetry, position: Position) : Boolean {
        // Reset completion
        if (verticalCompleted && horizontalCompleted) {
            verticalCompleted = false
            horizontalCompleted = false
            return false
        }

        robot.getRobotEncoderPositions(telemetry)
        robot.getRobotEncoderTargets(telemetry)

        // Move to position
        if (!verticalCompleted) {
            verticalCompleted = moveToPositionVertical(position.forwardDistance)
        }
        if (verticalCompleted && !horizontalCompleted) {
            horizontalCompleted = moveToPositionHorizontal(position.strafeDistance)
        }
        return verticalCompleted && horizontalCompleted
    }

    private fun moveToPositionVertical(forwardDistance: Double) : Boolean {
        // Move the robot to the specified position
        if (forwardDistance != 0.0 && !robot.driving()) {
            // Vertical movement (y-axis)
            robot.driveWithEncoder(0.6, forwardDistance)
        }
        return !robot.driving()  // Check for completion
    }

    private fun moveToPositionHorizontal(strafeDistance: Double) : Boolean {
        if (strafeDistance != 0.0 && !robot.driving()) {
            // Horizontal movement (x-axis)
            robot.strafeWithEncoder(0.6, strafeDistance)
        }
        return !robot.driving()  // Check for completion
    }

    private fun controlExtender(extend: Boolean) : Boolean {
        // Reset completion
        if (extenderCompleted) {
            extenderCompleted = false
            return false
        }
        // telemetry.addData("Lifter target position", "%d", 1)
        // Extend or retract the extender
        val completed = if (extend) robot.extendExtender() else robot.retractExtender()

        // Wait for completion
        if (completed) {
            extenderCompleted = true
            return true
        }
        return false
    }

    private fun liftLifters() : Boolean {
        // Reset completion
        if (liftersCompleted) {
            liftersCompleted = false
            robot.liftLifters()
        }

        robot.controllLifters(0.86)

        // Wait for completion
        if (!robot.liftersMoving()) {
            liftersCompleted = true
            return true
        }
        return false
    }
}