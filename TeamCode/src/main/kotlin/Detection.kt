package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.robots.Steve

@TeleOp(name = "Test - Detection", group = "Experimental")
class Detection : OpMode() {
    private val robot = Steve()

    override fun init() {
        robot.init(hardwareMap)
    }

    override fun loop() {
        robot.detectObjects(telemetry)
    }
}