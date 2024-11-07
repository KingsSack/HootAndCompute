package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import robot.Steve

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
        robot.driveWithGamepad(gamepad1)
        robot.controlClawWithGamepad(gamepad2)
    }
}