package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.robots.Steve

@TeleOp(name = "test-distance", group = "Experimental")
class Thingy : OpMode() {
    private val robot = Steve()

    override fun init() {
        robot.init(hardwareMap)
    }

    override fun loop() {
        robot.getDistanceToObject(telemetry)
        telemetry.update()
    }
}