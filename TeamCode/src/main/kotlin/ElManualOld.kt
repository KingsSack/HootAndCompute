package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp(name = "El Manual Old", group = "Competition")
class ElManualOld : OpMode() {
    // Robot
    private val robot = Fredrick()

    // Initialize
    override fun init() {
        robot.init(hardwareMap)
    }

    // Loop
    override fun loop() {
        // Drive
        robot.drive(gamepad1)

        // Attachments
        robot.arm.controller(gamepad2, robot.runtime)
        robot.hook.controller(gamepad2)
        robot.launcher.controller(gamepad2)
    }
}
