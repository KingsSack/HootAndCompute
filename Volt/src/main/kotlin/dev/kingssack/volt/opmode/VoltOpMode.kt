package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.robot.Robot

abstract class VoltOpMode<R : Robot>(robotFactory: (HardwareMap) -> R) : LinearOpMode() {
    protected val robot : R by lazy { robotFactory(hardwareMap) }

    /** Optional initialization code. */
    open fun initialize() {
        // Default implementation does nothing
    }

    /** Optional code to run when the op mode begins. */
    abstract fun begin()

    override fun runOpMode() {
        initialize()
        waitForStart()
        begin()
    }
}
