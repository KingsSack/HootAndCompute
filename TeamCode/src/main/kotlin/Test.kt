package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import robot.Goose

@TeleOp(name = "Test - Seal", group = "Experimental")
class Test : OpMode() {
    private val robot = Goose()

    override fun init() {
        robot.init(hardwareMap)
    }

    override fun loop() {
        robot.driveWithGamepad(gamepad1)
        robot.liftArmWithGamepad(gamepad2)
    }
}