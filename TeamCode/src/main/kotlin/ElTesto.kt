package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp(name = "El Testo", group = "Test")
abstract class ElTesto : OpMode() {
    private val robot = Bartholomew()

    override fun init() {
        robot.init(hardwareMap)
    }

    override fun loop() {
        robot.testMotor.power = gamepad1.left_stick_y.toDouble()
    }
}