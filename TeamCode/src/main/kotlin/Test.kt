package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.robots.Goose

@TeleOp(name = "Test - Seal", group = "Experimental")
abstract class Test : OpMode() {
    private val robot = Goose()

    override fun init() {
        robot.init(hardwareMap)
    }

    override fun loop() {
        robot.manualControl(gamepad1)
    }
}