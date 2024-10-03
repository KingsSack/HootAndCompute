package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.robots.Steve

@TeleOp(name = "Test - Distance", group = "Experimental")
class Distance : OpMode() {
    private val robot = Steve()

    override fun init() {
        robot.init(hardwareMap)
    }

    override fun loop() {
        robot.getDistanceToObstacle(telemetry)
        telemetry.update()
    }
}