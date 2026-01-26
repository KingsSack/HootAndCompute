package dev.kingssack.volt.ai

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot

abstract class AIOpMode<R: Robot>(robotFactory: (HardwareMap) -> R) : VoltOpMode<R>(robotFactory) {
    override fun begin() {
        telemetry.addData("Status", "Agent is running")
    }
}
