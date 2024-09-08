package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.robots.Fredrick

@TeleOp(name = "El Manual", group = "Competition")
class ElManual : OpMode() {
    // Choose active robot
    private val robot = Fredrick()

    // Initialize
    override fun init() {
        robot.init(hardwareMap)
    }

    // Loop
    override fun loop() {
        // Drive
        robot.drive(gamepad1)
    }
}