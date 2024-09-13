package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.robots.Steve

@TeleOp(name = "Manual - Whale", group = "Competition")
class Manual : OpMode() {
    // Robot
    private val robot = Steve()

    // Initialize
    override fun init() {
        robot.init(hardwareMap)
    }

    // Loop
    override fun loop() {
        // Drive
        robot.manualControl(gamepad1)
    }
}