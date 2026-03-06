package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.robot.Robot

/**
 * Base class for Volt's OpModes
 *
 * @param robotFactory a function that creates a [robot] instance from a HardwareMap
 * @param R the type of robot
 * @property robot the robot instance
 */
abstract class VoltOpMode<R : Robot>(robotFactory: (HardwareMap) -> R) : LinearOpMode() {
    protected val robot: R by lazy { robotFactory(hardwareMap) }

    /** Optional initialization code. */
    open fun initialize() {}

    /** Optional code to run when the op mode begins. */
    abstract fun begin()

    /** Optional code to run when the op mode ends. */
    open fun end() {}

    override fun runOpMode() {
        with(telemetry) {
            addData("Status", "Initializing...")
            update()
            initialize()

            addData("Status", "Ready")
            update()
            waitForStart()

            addData("Status", "Running")
            update()
            begin()

            addData("Status", "Finished")
            update()
            end()
        }
    }
}
