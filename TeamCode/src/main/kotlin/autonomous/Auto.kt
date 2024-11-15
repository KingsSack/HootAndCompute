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
    private var extenderCompleted = true
    // private var clawCompleted = true
    private var liftersCompleted = true

    // Strikes
    private var strikes = 0

    override fun run(telemetry: Telemetry) {
        telemetry.addData("State", state.toString())
        telemetry.addData("Current Sample", currentSampleIndex + 1)

        // Check for obstacles
        checkForObstacles(telemetry)

        when (state) {
            State.GO_TO_SAMPLE -> {
                // Move to the sample position
                val position = samplePositions[currentSampleIndex]
                if (!moveToPosition(position)) return
                state = State.COLLECT_SAMPLE
            }
            State.COLLECT_SAMPLE -> {
                // Collect the sample
                if (!collectSample()) return
                state = State.GO_TO_BASKET
            }
            State.GO_TO_BASKET -> {
                // Move to the basket position
                if (!moveToPosition(basketPosition)) return
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
                if (!moveToPosition(observationZonePosition)) return
                state = State.STOP
            }
            State.STOP -> {
                // Stop the robot
                robot.halt()
            }
        }
        telemetry.update()
    }

    private fun checkForObstacles(telemetry: Telemetry) {
        // Check for obstacles less than 300 mm away
        if (robot.getDistanceToObstacle(telemetry) < 300) {
            strikes++
            telemetry.addData("Strikes", strikes)
            if (strikes >= 5) {
                state = State.STOP
            }
        }
    }

    private fun moveToPosition(position: Position) : Boolean {
        // Move the robot to the specified position
        if (position.forwardDistance != 0.0) {
            // Vertical movement (y-axis)
            robot.driveWithEncoder(0.9, position.forwardDistance)
        }
        if (position.strafeDistance != 0.0) {
            // Horizontal movement (x-axis)
            robot.strafeWithEncoder(0.9, position.strafeDistance)
        }
        if (robot.driving()) return false  // Check for completion
        return true
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

    private fun controlExtender(extend: Boolean) : Boolean {
        if (extenderCompleted) {
            extenderCompleted = false
            return false
        }
        
        val completed = if (extend) robot.extendExtender() else robot.retractExtender()
        if (completed) {
            extenderCompleted = true
            return true
        }
        return false
    }

    private fun liftLifters() : Boolean {
        if (liftersCompleted) {
            liftersCompleted = false
            robot.liftLifters(0.86)
            return false
        }

        if (!robot.liftersMoving()) {
            liftersCompleted = true
            return true
        }
        return false
    }
}